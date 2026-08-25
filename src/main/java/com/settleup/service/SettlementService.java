package com.settleup.service;

import com.settleup.dto.settlement.CreateSettlementRequest;
import com.settleup.dto.settlement.GroupBalancesResponse;
import com.settleup.dto.settlement.SettlementDto;
import com.settleup.dto.settlement.SettlementPlanDto;
import com.settleup.dto.settlement.UserBalanceDto;
import com.settleup.dto.user.UserDto;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.SettlementEntity;
import com.settleup.entity.UserEntity;
import com.settleup.enums.SettlementStatus;
import com.settleup.exception.BusinessRuleException;
import com.settleup.exception.ResourceNotFoundException;
import com.settleup.exception.UnauthorizedAccessException;
import com.settleup.mapper.SettlementMapper;
import com.settleup.repository.GroupRepository;
import com.settleup.repository.SettlementRepository;
import com.settleup.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final SettlementMapper settlementMapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<SettlementPlanDto> computeSettlementPlan(Long groupId) {
        GroupBalancesResponse balancesResponse = balanceService.getGroupBalances(groupId);
        return computeSettlementPlanFromBalances(balancesResponse.getBalances());
    }

    @Transactional(readOnly = true)
    public List<SettlementDto> getGroupSettlements(Long groupId) {
        return settlementRepository.findByGroupId(groupId).stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Pure, unit-testable greedy settlement plan calculation.
     */
    public List<SettlementPlanDto> computeSettlementPlanFromBalances(List<UserBalanceDto> userBalances) {
        PriorityQueue<BalanceHolder> creditors = new PriorityQueue<>(
                (a, b) -> b.getAmount().compareTo(a.getAmount())
        );

        PriorityQueue<BalanceHolder> debtors = new PriorityQueue<>(
                (a, b) -> b.getAmount().compareTo(a.getAmount()) // compare by absolute debt amount
        );

        BigDecimal epsilon = new BigDecimal("0.005");

        for (UserBalanceDto b : userBalances) {
            BigDecimal net = b.getNetBalance().setScale(2, RoundingMode.HALF_UP);
            if (net.compareTo(epsilon) > 0) {
                creditors.add(new BalanceHolder(b.getUser(), net));
            } else if (net.compareTo(epsilon.negate()) < 0) {
                debtors.add(new BalanceHolder(b.getUser(), net.abs()));
            }
        }

        List<SettlementPlanDto> plan = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            BalanceHolder creditor = creditors.poll();
            BalanceHolder debtor = debtors.poll();

            BigDecimal settleAmount = creditor.getAmount().min(debtor.getAmount())
                    .setScale(2, RoundingMode.HALF_UP);

            plan.add(SettlementPlanDto.builder()
                    .fromUser(debtor.getUser())
                    .toUser(creditor.getUser())
                    .amount(settleAmount)
                    .build());

            BigDecimal remainingCredit = creditor.getAmount().subtract(settleAmount);
            BigDecimal remainingDebt = debtor.getAmount().subtract(settleAmount);

            if (remainingCredit.compareTo(epsilon) > 0) {
                creditor.setAmount(remainingCredit);
                creditors.add(creditor);
            }

            if (remainingDebt.compareTo(epsilon) > 0) {
                debtor.setAmount(remainingDebt);
                debtors.add(debtor);
            }
        }

        return plan;
    }

    @Transactional
    public SettlementDto createSettlement(Long groupId, CreateSettlementRequest request, UserEntity currentUser) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        UserEntity paidTo = userRepository.findById(request.getPaidToId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found with id: " + request.getPaidToId()));

        if (currentUser.getId().equals(paidTo.getId())) {
            throw new BusinessRuleException("Cannot record a settlement to yourself");
        }

        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);

        // Idempotency check: guard against duplicate PENDING settlements within 1 minute
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1);
        List<SettlementEntity> recentSettlements = settlementRepository.findByGroupId(groupId);
        boolean duplicateExists = recentSettlements.stream().anyMatch(s ->
                s.getPaidBy().getId().equals(currentUser.getId()) &&
                s.getPaidTo().getId().equals(paidTo.getId()) &&
                s.getAmount().compareTo(amount) == 0 &&
                s.getStatus() == SettlementStatus.PENDING &&
                s.getCreatedAt().isAfter(cutoff)
        );

        if (duplicateExists) {
            throw new BusinessRuleException("A duplicate pending settlement was created recently. Please wait before retrying.");
        }

        SettlementEntity settlement = SettlementEntity.builder()
                .group(group)
                .paidBy(currentUser)
                .paidTo(paidTo)
                .amount(amount)
                .note(request.getNote())
                .status(SettlementStatus.PENDING)
                .date(LocalDateTime.now())
                .build();

        settlement = settlementRepository.save(settlement);

        auditLogService.logChange("Settlement", settlement.getId(), "CREATE", currentUser, null, settlement.getId());

        return mapToDto(settlement);
    }

    @Transactional
    public SettlementDto confirmSettlement(Long settlementId, UserEntity currentUser) {
        SettlementEntity settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + settlementId));

        if (!settlement.getPaidTo().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("Only the receiver (" + settlement.getPaidTo().getName() + ") can confirm this settlement");
        }

        if (settlement.getStatus() == SettlementStatus.CONFIRMED) {
            throw new BusinessRuleException("Settlement is already confirmed");
        }

        SettlementStatus oldStatus = settlement.getStatus();
        settlement.setStatus(SettlementStatus.CONFIRMED);
        settlement = settlementRepository.save(settlement);

        auditLogService.logChange("Settlement", settlement.getId(), "CONFIRM", currentUser,
                "Status: " + oldStatus, "Status: " + SettlementStatus.CONFIRMED);

        return mapToDto(settlement);
    }

    public SettlementDto mapToDto(SettlementEntity entity) {
        return settlementMapper.toDto(entity);
    }

    @Data
    @AllArgsConstructor
    private static class BalanceHolder {
        private UserDto user;
        private BigDecimal amount;
    }
}
