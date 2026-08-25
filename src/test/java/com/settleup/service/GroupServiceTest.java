package com.settleup.service;

import com.settleup.dto.group.GroupDto;
import com.settleup.dto.group.GroupMemberDto;
import com.settleup.dto.user.UserDto;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.GroupMemberEntity;
import com.settleup.entity.UserEntity;
import com.settleup.enums.GroupRole;
import com.settleup.enums.MemberStatus;
import com.settleup.enums.Role;
import com.settleup.exception.BusinessRuleException;
import com.settleup.mapper.GroupMapper;
import com.settleup.repository.GroupMemberRepository;
import com.settleup.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GroupServiceTest {

    private GroupService groupService;
    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;
    private GroupMapper groupMapper;
    private BalanceService balanceService;
    private AuditLogService auditLogService;

    private UserEntity userA;
    private GroupEntity mockGroup;
    private GroupMemberEntity mockMember;
    private UserDto userDtoA;
    private GroupDto mockGroupDto;
    private GroupMemberDto mockMemberDto;

    @BeforeEach
    void setUp() {
        groupRepository = Mockito.mock(GroupRepository.class);
        groupMemberRepository = Mockito.mock(GroupMemberRepository.class);
        groupMapper = Mockito.mock(GroupMapper.class);
        balanceService = Mockito.mock(BalanceService.class);
        auditLogService = Mockito.mock(AuditLogService.class);

        groupService = new GroupService(groupRepository, groupMemberRepository, null, groupMapper, balanceService, auditLogService);

        userA = UserEntity.builder().id(1L).name("Alice").email("alice@example.com").role(Role.USER).build();
        userDtoA = UserDto.builder().id(1L).name("Alice").email("alice@example.com").role(Role.USER).build();

        mockGroup = GroupEntity.builder().id(10L).name("Trip").currency("USD").isArchived(false).build();
        mockMember = GroupMemberEntity.builder().id(100L).group(mockGroup).user(userA).roleInGroup(GroupRole.MEMBER).status(MemberStatus.ACTIVE).build();

        mockGroupDto = GroupDto.builder().id(10L).name("Trip").currency("USD").isArchived(false).build();
        mockMemberDto = GroupMemberDto.builder().id(100L).user(userDtoA).roleInGroup(GroupRole.MEMBER).status(MemberStatus.ACTIVE).build();

        when(groupRepository.findById(10L)).thenReturn(Optional.of(mockGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(10L, 1L)).thenReturn(Optional.of(mockMember));
        when(groupMapper.toDtoWithoutMembers(any())).thenReturn(mockGroupDto);
        when(groupMapper.toMemberDtoList(any())).thenReturn(Collections.singletonList(mockMemberDto));
    }

    @Test
    @DisplayName("Removing member with non-zero balance throws BusinessRuleException")
    void testRemoveMemberWithNonZeroBalance() {
        when(balanceService.getUserNetBalanceInGroup(10L, 1L)).thenReturn(new BigDecimal("25.00"));

        assertThrows(BusinessRuleException.class, () ->
                groupService.removeMember(10L, 1L, userA)
        );
    }

    @Test
    @DisplayName("Removing member with zero balance marks status as LEFT and sets leftAt timestamp")
    void testRemoveMemberWithZeroBalance() {
        when(balanceService.getUserNetBalanceInGroup(10L, 1L)).thenReturn(new BigDecimal("0.00"));
        when(groupMemberRepository.findByGroupId(10L)).thenReturn(Collections.singletonList(mockMember));

        groupService.removeMember(10L, 1L, userA);

        assertEquals(MemberStatus.LEFT, mockMember.getStatus());
        assertNotNull(mockMember.getLeftAt());
    }
}
