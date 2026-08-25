package com.settleup.service;

import com.settleup.dto.settlement.SettlementPlanDto;
import com.settleup.dto.settlement.UserBalanceDto;
import com.settleup.dto.user.UserDto;
import com.settleup.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SettlementServiceTest {

    private SettlementService settlementService;

    private UserDto userA;
    private UserDto userB;
    private UserDto userC;
    private UserDto userD;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(null, null, null, null, null, null);

        userA = UserDto.builder().id(1L).name("Alice").email("alice@example.com").role(Role.USER).build();
        userB = UserDto.builder().id(2L).name("Bob").email("bob@example.com").role(Role.USER).build();
        userC = UserDto.builder().id(3L).name("Charlie").email("charlie@example.com").role(Role.USER).build();
        userD = UserDto.builder().id(4L).name("David").email("david@example.com").role(Role.USER).build();
    }

    @Test
    @DisplayName("1. Simple 2-person settlement: Alice owes Bob $50")
    void testTwoPersonSettlement() {
        // Alice: netBalance = -50.00 (owes $50)
        // Bob: netBalance = +50.00 (owed $50)
        UserBalanceDto balanceA = UserBalanceDto.builder().user(userA).netBalance(new BigDecimal("-50.00")).build();
        UserBalanceDto balanceB = UserBalanceDto.builder().user(userB).netBalance(new BigDecimal("50.00")).build();

        List<SettlementPlanDto> plan = settlementService.computeSettlementPlanFromBalances(Arrays.asList(balanceA, balanceB));

        assertNotNull(plan);
        assertEquals(1, plan.size());

        SettlementPlanDto item = plan.get(0);
        assertEquals(userA.getId(), item.getFromUser().getId());
        assertEquals(userB.getId(), item.getToUser().getId());
        assertEquals(0, new BigDecimal("50.00").compareTo(item.getAmount()));
    }

    @Test
    @DisplayName("2. 3-person cycle: A owes B $20, B owes C $20, C owes A $20 -> net balances are all 0 -> 0 transfers")
    void testThreePersonCycleSettlement() {
        // Alice paid $20, owes $20 to C -> net balance = 0.00
        // Bob paid $20, owes $20 to A -> net balance = 0.00
        // Charlie paid $20, owes $20 to B -> net balance = 0.00
        UserBalanceDto balanceA = UserBalanceDto.builder().user(userA).netBalance(new BigDecimal("0.00")).build();
        UserBalanceDto balanceB = UserBalanceDto.builder().user(userB).netBalance(new BigDecimal("0.00")).build();
        UserBalanceDto balanceC = UserBalanceDto.builder().user(userC).netBalance(new BigDecimal("0.00")).build();

        List<SettlementPlanDto> plan = settlementService.computeSettlementPlanFromBalances(Arrays.asList(balanceA, balanceB, balanceC));

        assertNotNull(plan);
        assertTrue(plan.isEmpty(), "Cycle balances result in 0 transfers");
    }

    @Test
    @DisplayName("3. Already-settled group: Everyone net balance is 0.00 -> returns empty list")
    void testAlreadySettledGroup() {
        UserBalanceDto balanceA = UserBalanceDto.builder().user(userA).netBalance(BigDecimal.ZERO).build();
        UserBalanceDto balanceB = UserBalanceDto.builder().user(userB).netBalance(BigDecimal.ZERO).build();

        List<SettlementPlanDto> plan = settlementService.computeSettlementPlanFromBalances(Arrays.asList(balanceA, balanceB));

        assertNotNull(plan);
        assertTrue(plan.isEmpty());
    }

    @Test
    @DisplayName("4. Complex 4-person group: A(+60), B(-30), C(-20), D(-10) -> minimal 3 transactions to A")
    void testComplexMultiPersonGroupSettlement() {
        UserBalanceDto balanceA = UserBalanceDto.builder().user(userA).netBalance(new BigDecimal("60.00")).build();
        UserBalanceDto balanceB = UserBalanceDto.builder().user(userB).netBalance(new BigDecimal("-30.00")).build();
        UserBalanceDto balanceC = UserBalanceDto.builder().user(userC).netBalance(new BigDecimal("-20.00")).build();
        UserBalanceDto balanceD = UserBalanceDto.builder().user(userD).netBalance(new BigDecimal("-10.00")).build();

        List<SettlementPlanDto> plan = settlementService.computeSettlementPlanFromBalances(
                Arrays.asList(balanceA, balanceB, balanceC, balanceD)
        );

        assertNotNull(plan);
        assertEquals(3, plan.size());

        BigDecimal totalTransferred = plan.stream()
                .map(SettlementPlanDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("60.00").compareTo(totalTransferred));
        assertTrue(plan.stream().allMatch(p -> p.getToUser().getId().equals(userA.getId())));
    }
}
