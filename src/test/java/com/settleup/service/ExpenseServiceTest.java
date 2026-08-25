package com.settleup.service;

import com.settleup.dto.expense.SplitInputDto;
import com.settleup.entity.ExpenseEntity;
import com.settleup.entity.ExpenseSplitEntity;
import com.settleup.entity.UserEntity;
import com.settleup.enums.Role;
import com.settleup.enums.SplitType;
import com.settleup.exception.BusinessRuleException;
import com.settleup.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class ExpenseServiceTest {

    private ExpenseService expenseService;
    private UserRepository userRepository;

    private UserEntity userA;
    private UserEntity userB;
    private UserEntity userC;
    private ExpenseEntity mockExpense;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        expenseService = new ExpenseService(null, null, null, userRepository, null, null);

        userA = UserEntity.builder().id(1L).name("Alice").email("alice@example.com").role(Role.USER).build();
        userB = UserEntity.builder().id(2L).name("Bob").email("bob@example.com").role(Role.USER).build();
        userC = UserEntity.builder().id(3L).name("Charlie").email("charlie@example.com").role(Role.USER).build();

        mockExpense = ExpenseEntity.builder().id(100L).totalAmount(new BigDecimal("100.00")).paidBy(userA).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(userRepository.findById(3L)).thenReturn(Optional.of(userC));
    }

    @Test
    @DisplayName("Equal split rounding: $100.00 split among 3 members -> $33.33 to non-payers, $33.34 to payer")
    void testEqualSplitRounding() {
        List<SplitInputDto> splits = Arrays.asList(
                SplitInputDto.builder().userId(1L).build(),
                SplitInputDto.builder().userId(2L).build(),
                SplitInputDto.builder().userId(3L).build()
        );

        List<ExpenseSplitEntity> result = expenseService.calculateAndCreateSplits(
                mockExpense, SplitType.EQUAL, new BigDecimal("100.00"), 1L, splits
        );

        assertEquals(3, result.size());

        BigDecimal sum = result.stream()
                .map(ExpenseSplitEntity::getAmountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("100.00").compareTo(sum), "Sum of splits must equal total amount");

        ExpenseSplitEntity payerSplit = result.stream().filter(s -> s.getUser().getId().equals(1L)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("33.34").compareTo(payerSplit.getAmountOwed()), "Payer gets remainder penny");
    }

    @Test
    @DisplayName("Exact split validation: sum mismatch throws BusinessRuleException")
    void testExactSplitValidation() {
        List<SplitInputDto> splits = Arrays.asList(
                SplitInputDto.builder().userId(1L).splitValue(new BigDecimal("40.00")).build(),
                SplitInputDto.builder().userId(2L).splitValue(new BigDecimal("50.00")).build()
        );

        assertThrows(BusinessRuleException.class, () ->
                expenseService.calculateAndCreateSplits(
                        mockExpense, SplitType.EXACT, new BigDecimal("100.00"), 1L, splits
                )
        );
    }

    @Test
    @DisplayName("Percentage split validation: non-100 sum throws BusinessRuleException")
    void testPercentageSplitValidation() {
        List<SplitInputDto> splits = Arrays.asList(
                SplitInputDto.builder().userId(1L).splitValue(new BigDecimal("50.00")).build(),
                SplitInputDto.builder().userId(2L).splitValue(new BigDecimal("40.00")).build()
        );

        assertThrows(BusinessRuleException.class, () ->
                expenseService.calculateAndCreateSplits(
                        mockExpense, SplitType.PERCENTAGE, new BigDecimal("100.00"), 1L, splits
                )
        );
    }

    @Test
    @DisplayName("Shares split calculation: 2 shares + 1 share for $120.00 -> $80.00 and $40.00")
    void testSharesSplitCalculation() {
        ExpenseEntity expense = ExpenseEntity.builder().id(101L).totalAmount(new BigDecimal("120.00")).paidBy(userA).build();

        List<SplitInputDto> splits = Arrays.asList(
                SplitInputDto.builder().userId(1L).splitValue(new BigDecimal("2")).build(),
                SplitInputDto.builder().userId(2L).splitValue(new BigDecimal("1")).build()
        );

        List<ExpenseSplitEntity> result = expenseService.calculateAndCreateSplits(
                expense, SplitType.SHARES, new BigDecimal("120.00"), 1L, splits
        );

        assertEquals(2, result.size());

        ExpenseSplitEntity userASplit = result.stream().filter(s -> s.getUser().getId().equals(1L)).findFirst().orElseThrow();
        ExpenseSplitEntity userBSplit = result.stream().filter(s -> s.getUser().getId().equals(2L)).findFirst().orElseThrow();

        assertEquals(0, new BigDecimal("80.00").compareTo(userASplit.getAmountOwed()));
        assertEquals(0, new BigDecimal("40.00").compareTo(userBSplit.getAmountOwed()));
    }
}
