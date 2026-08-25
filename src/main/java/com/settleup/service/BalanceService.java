package com.settleup.service;

import com.settleup.dto.settlement.GroupBalancesResponse;
import com.settleup.dto.settlement.UserBalanceDto;
import com.settleup.entity.ExpenseEntity;
import com.settleup.entity.ExpenseSplitEntity;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.GroupMemberEntity;
import com.settleup.entity.SettlementEntity;
import com.settleup.enums.SettlementStatus;
import com.settleup.exception.ResourceNotFoundException;
import com.settleup.mapper.UserMapper;
import com.settleup.repository.ExpenseRepository;
import com.settleup.repository.ExpenseSplitRepository;
import com.settleup.repository.GroupMemberRepository;
import com.settleup.repository.GroupRepository;
import com.settleup.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final SettlementRepository settlementRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public GroupBalancesResponse getGroupBalances(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(groupId);
        List<ExpenseEntity> activeExpenses = expenseRepository.findByGroupIdAndIsDeletedFalse(groupId);
        List<ExpenseSplitEntity> activeSplits = expenseSplitRepository.findActiveSplitsByGroupId(groupId);
        List<SettlementEntity> confirmedSettlements = settlementRepository.findByGroupIdAndStatus(groupId, SettlementStatus.CONFIRMED);

        List<UserBalanceDto> balances = calculateGroupBalances(members, activeExpenses, activeSplits, confirmedSettlements);

        return GroupBalancesResponse.builder()
                .groupId(groupId)
                .currency(group.getCurrency())
                .balances(balances)
                .build();
    }

    /**
     * Pure, unit-testable computation of user net balances.
     */
    public List<UserBalanceDto> calculateGroupBalances(
            List<GroupMemberEntity> members,
            List<ExpenseEntity> activeExpenses,
            List<ExpenseSplitEntity> activeSplits,
            List<SettlementEntity> confirmedSettlements) {

        Map<Long, BigDecimal> totalPaidMap = new HashMap<>();
        Map<Long, BigDecimal> totalOwedMap = new HashMap<>();
        Map<Long, BigDecimal> settlementsSentMap = new HashMap<>();
        Map<Long, BigDecimal> settlementsReceivedMap = new HashMap<>();

        // Initialize maps for all members
        for (GroupMemberEntity m : members) {
            Long userId = m.getUser().getId();
            totalPaidMap.put(userId, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            totalOwedMap.put(userId, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            settlementsSentMap.put(userId, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            settlementsReceivedMap.put(userId, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        }

        // Sum amounts paid for expenses
        for (ExpenseEntity e : activeExpenses) {
            Long payerId = e.getPaidBy().getId();
            totalPaidMap.put(payerId, totalPaidMap.getOrDefault(payerId, BigDecimal.ZERO).add(e.getTotalAmount()));
        }

        // Sum amounts owed in splits
        for (ExpenseSplitEntity s : activeSplits) {
            Long userId = s.getUser().getId();
            totalOwedMap.put(userId, totalOwedMap.getOrDefault(userId, BigDecimal.ZERO).add(s.getAmountOwed()));
        }

        // Sum confirmed settlements
        for (SettlementEntity st : confirmedSettlements) {
            Long payerId = st.getPaidBy().getId();
            Long receiverId = st.getPaidTo().getId();

            settlementsSentMap.put(payerId, settlementsSentMap.getOrDefault(payerId, BigDecimal.ZERO).add(st.getAmount()));
            settlementsReceivedMap.put(receiverId, settlementsReceivedMap.getOrDefault(receiverId, BigDecimal.ZERO).add(st.getAmount()));
        }

        List<UserBalanceDto> result = new ArrayList<>();

        for (GroupMemberEntity m : members) {
            Long userId = m.getUser().getId();
            BigDecimal paid = totalPaidMap.getOrDefault(userId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal owed = totalOwedMap.getOrDefault(userId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sent = settlementsSentMap.getOrDefault(userId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal received = settlementsReceivedMap.getOrDefault(userId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

            // netBalance = paid - owed + sent - received
            BigDecimal netBalance = paid.subtract(owed).add(sent).subtract(received).setScale(2, RoundingMode.HALF_UP);

            result.add(UserBalanceDto.builder()
                    .user(userMapper.toDto(m.getUser()))
                    .netBalance(netBalance)
                    .totalPaid(paid)
                    .totalOwed(owed)
                    .settlementsSent(sent)
                    .settlementsReceived(received)
                    .build());
        }

        return result;
    }

    public BigDecimal getUserNetBalanceInGroup(Long groupId, Long userId) {
        GroupBalancesResponse response = getGroupBalances(groupId);
        return response.getBalances().stream()
                .filter(b -> b.getUser().getId().equals(userId))
                .map(UserBalanceDto::getNetBalance)
                .findFirst()
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
}
