package com.settleup.service;

import com.settleup.dto.expense.*;
import com.settleup.entity.ExpenseEntity;
import com.settleup.entity.ExpenseSplitEntity;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.UserEntity;
import com.settleup.enums.ExpenseCategory;
import com.settleup.enums.SplitType;
import com.settleup.exception.BusinessRuleException;
import com.settleup.exception.ResourceNotFoundException;
import com.settleup.mapper.ExpenseMapper;
import com.settleup.repository.ExpenseRepository;
import com.settleup.repository.ExpenseSplitRepository;
import com.settleup.repository.GroupRepository;
import com.settleup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public ExpenseDto createExpense(Long groupId, CreateExpenseRequest request, UserEntity currentUser) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (group.getIsArchived()) {
            throw new BusinessRuleException("Cannot add expenses to an archived group");
        }

        UserEntity paidBy = userRepository.findById(request.getPaidById())
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found with id: " + request.getPaidById()));

        BigDecimal totalAmount = request.getTotalAmount().setScale(2, RoundingMode.HALF_UP);

        ExpenseEntity expense = ExpenseEntity.builder()
                .group(group)
                .description(request.getDescription())
                .totalAmount(totalAmount)
                .paidBy(paidBy)
                .category(request.getCategory())
                .date(request.getDate() != null ? request.getDate() : LocalDateTime.now())
                .createdBy(currentUser)
                .isDeleted(false)
                .notes(request.getNotes())
                .build();

        expense = expenseRepository.save(expense);

        List<ExpenseSplitEntity> splits = calculateAndCreateSplits(expense, request.getSplitType(), totalAmount, request.getPaidById(), request.getSplits());
        expenseSplitRepository.saveAll(splits);

        auditLogService.logChange("Expense", expense.getId(), "CREATE", currentUser, null, expense.getDescription());

        return mapToDto(expense, splits);
    }

    @Transactional
    public ExpenseDto updateExpense(Long expenseId, UpdateExpenseRequest request, UserEntity currentUser) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (expense.getIsDeleted()) {
            throw new BusinessRuleException("Cannot edit a deleted expense");
        }

        if (expense.getGroup().getIsArchived()) {
            throw new BusinessRuleException("Cannot edit expenses in an archived group");
        }

        UserEntity paidBy = userRepository.findById(request.getPaidById())
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found with id: " + request.getPaidById()));

        BigDecimal oldAmount = expense.getTotalAmount();
        String oldDescription = expense.getDescription();

        BigDecimal newTotalAmount = request.getTotalAmount().setScale(2, RoundingMode.HALF_UP);

        expense.setDescription(request.getDescription());
        expense.setTotalAmount(newTotalAmount);
        expense.setPaidBy(paidBy);
        expense.setCategory(request.getCategory());
        if (request.getDate() != null) {
            expense.setDate(request.getDate());
        }
        expense.setNotes(request.getNotes());

        expense = expenseRepository.save(expense);

        // Delete old splits and re-create
        expenseSplitRepository.deleteByExpenseId(expenseId);

        List<ExpenseSplitEntity> newSplits = calculateAndCreateSplits(expense, request.getSplitType(), newTotalAmount, request.getPaidById(), request.getSplits());
        expenseSplitRepository.saveAll(newSplits);

        auditLogService.logChange("Expense", expense.getId(), "UPDATE", currentUser,
                "Desc: " + oldDescription + ", Amount: " + oldAmount,
                "Desc: " + expense.getDescription() + ", Amount: " + newTotalAmount);

        return mapToDto(expense, newSplits);
    }

    @Transactional
    public void deleteExpense(Long expenseId, UserEntity currentUser) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (expense.getIsDeleted()) {
            throw new BusinessRuleException("Expense is already deleted");
        }

        expense.setIsDeleted(true);
        expenseRepository.save(expense);

        auditLogService.logChange("Expense", expenseId, "DELETE", currentUser,
                "isDeleted: false", "isDeleted: true");
    }

    @Transactional(readOnly = true)
    public Page<ExpenseDto> getGroupExpenses(
            Long groupId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ExpenseCategory category,
            Long paidById,
            Pageable pageable) {

        return expenseRepository.findGroupExpensesFiltered(groupId, startDate, endDate, category, paidById, pageable)
                .map(e -> {
                    List<ExpenseSplitEntity> splits = expenseSplitRepository.findByExpenseId(e.getId());
                    return mapToDto(e, splits);
                });
    }

    @Transactional(readOnly = true)
    public Page<ExpenseDto> getAllExpenses(Pageable pageable) {
        return expenseRepository.findAll(pageable)
                .map(e -> {
                    List<ExpenseSplitEntity> splits = expenseSplitRepository.findByExpenseId(e.getId());
                    return mapToDto(e, splits);
                });
    }

    @Transactional(readOnly = true)
    public ExpenseDto getExpenseById(Long expenseId) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));
        List<ExpenseSplitEntity> splits = expenseSplitRepository.findByExpenseId(expense.getId());
        return mapToDto(expense, splits);
    }

    /**
     * Pure calculation helper for parsing and validating splits.
     */
    public List<ExpenseSplitEntity> calculateAndCreateSplits(
            ExpenseEntity expense,
            SplitType splitType,
            BigDecimal totalAmount,
            Long paidById,
            List<SplitInputDto> splitInputs) {

        if (splitInputs == null || splitInputs.isEmpty()) {
            throw new BusinessRuleException("Expense must have at least one split member");
        }

        int count = splitInputs.size();
        List<ExpenseSplitEntity> result = new ArrayList<>();
        BigDecimal epsilon = new BigDecimal("0.01");

        switch (splitType) {
            case EQUAL: {
                BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                BigDecimal totalAssigned = baseAmount.multiply(BigDecimal.valueOf(count));
                BigDecimal remainder = totalAmount.subtract(totalAssigned);

                for (int i = 0; i < count; i++) {
                    SplitInputDto input = splitInputs.get(i);
                    UserEntity user = userRepository.findById(input.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Split user not found with id: " + input.getUserId()));

                    BigDecimal amountOwed = baseAmount;
                    // Assign remainder (leftover penny/paisa) deterministically to payer if present in split, or first user
                    if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                        boolean isPayer = user.getId().equals(paidById);
                        boolean isFirst = (i == 0);
                        boolean payerInSplits = splitInputs.stream().anyMatch(s -> s.getUserId().equals(paidById));

                        if ((payerInSplits && isPayer) || (!payerInSplits && isFirst)) {
                            amountOwed = amountOwed.add(remainder);
                            remainder = BigDecimal.ZERO;
                        }
                    }

                    result.add(ExpenseSplitEntity.builder()
                            .expense(expense)
                            .user(user)
                            .splitType(SplitType.EQUAL)
                            .amountOwed(amountOwed)
                            .splitValue(baseAmount)
                            .build());
                }
                break;
            }
            case EXACT: {
                BigDecimal sumExact = BigDecimal.ZERO;
                for (SplitInputDto input : splitInputs) {
                    if (input.getSplitValue() == null || input.getSplitValue().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BusinessRuleException("Exact split value must be positive for user id: " + input.getUserId());
                    }
                    sumExact = sumExact.add(input.getSplitValue());
                }

                if (sumExact.subtract(totalAmount).abs().compareTo(epsilon) > 0) {
                    throw new BusinessRuleException("Sum of exact splits (" + sumExact + ") must equal total expense amount (" + totalAmount + ")");
                }

                for (SplitInputDto input : splitInputs) {
                    UserEntity user = userRepository.findById(input.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Split user not found with id: " + input.getUserId()));

                    result.add(ExpenseSplitEntity.builder()
                            .expense(expense)
                            .user(user)
                            .splitType(SplitType.EXACT)
                            .amountOwed(input.getSplitValue().setScale(2, RoundingMode.HALF_UP))
                            .splitValue(input.getSplitValue())
                            .build());
                }
                break;
            }
            case PERCENTAGE: {
                BigDecimal sumPercentage = BigDecimal.ZERO;
                for (SplitInputDto input : splitInputs) {
                    if (input.getSplitValue() == null || input.getSplitValue().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BusinessRuleException("Percentage value must be positive for user id: " + input.getUserId());
                    }
                    sumPercentage = sumPercentage.add(input.getSplitValue());
                }

                if (sumPercentage.subtract(new BigDecimal("100.00")).abs().compareTo(epsilon) > 0) {
                    throw new BusinessRuleException("Sum of percentage splits (" + sumPercentage + "%) must equal 100%");
                }

                BigDecimal calculatedTotal = BigDecimal.ZERO;
                for (int i = 0; i < count; i++) {
                    SplitInputDto input = splitInputs.get(i);
                    UserEntity user = userRepository.findById(input.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Split user not found with id: " + input.getUserId()));

                    BigDecimal amountOwed = totalAmount.multiply(input.getSplitValue())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    calculatedTotal = calculatedTotal.add(amountOwed);

                    result.add(ExpenseSplitEntity.builder()
                            .expense(expense)
                            .user(user)
                            .splitType(SplitType.PERCENTAGE)
                            .amountOwed(amountOwed)
                            .splitValue(input.getSplitValue())
                            .build());
                }

                // Adjust leftover cents on first split item
                BigDecimal roundingDiff = totalAmount.subtract(calculatedTotal);
                if (roundingDiff.compareTo(BigDecimal.ZERO) != 0 && !result.isEmpty()) {
                    ExpenseSplitEntity first = result.get(0);
                    first.setAmountOwed(first.getAmountOwed().add(roundingDiff));
                }
                break;
            }
            case SHARES: {
                BigDecimal totalShares = BigDecimal.ZERO;
                for (SplitInputDto input : splitInputs) {
                    if (input.getSplitValue() == null || input.getSplitValue().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BusinessRuleException("Share count must be positive for user id: " + input.getUserId());
                    }
                    totalShares = totalShares.add(input.getSplitValue());
                }

                if (totalShares.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessRuleException("Total shares count must be greater than zero");
                }

                BigDecimal calculatedTotal = BigDecimal.ZERO;
                for (int i = 0; i < count; i++) {
                    SplitInputDto input = splitInputs.get(i);
                    UserEntity user = userRepository.findById(input.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Split user not found with id: " + input.getUserId()));

                    BigDecimal amountOwed = totalAmount.multiply(input.getSplitValue())
                            .divide(totalShares, 2, RoundingMode.HALF_UP);
                    calculatedTotal = calculatedTotal.add(amountOwed);

                    result.add(ExpenseSplitEntity.builder()
                            .expense(expense)
                            .user(user)
                            .splitType(SplitType.SHARES)
                            .amountOwed(amountOwed)
                            .splitValue(input.getSplitValue())
                            .build());
                }

                // Adjust leftover cents on first split item
                BigDecimal roundingDiff = totalAmount.subtract(calculatedTotal);
                if (roundingDiff.compareTo(BigDecimal.ZERO) != 0 && !result.isEmpty()) {
                    ExpenseSplitEntity first = result.get(0);
                    first.setAmountOwed(first.getAmountOwed().add(roundingDiff));
                }
                break;
            }
        }

        return result;
    }

    public ExpenseDto mapToDto(ExpenseEntity entity, List<ExpenseSplitEntity> splits) {
        ExpenseDto dto = expenseMapper.toDto(entity);
        dto.setSplits(expenseMapper.toSplitDtoList(splits));
        return dto;
    }
}
