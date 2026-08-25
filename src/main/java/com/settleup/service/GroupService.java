package com.settleup.service;

import com.settleup.dto.group.AddMemberRequest;
import com.settleup.dto.group.CreateGroupRequest;
import com.settleup.dto.group.GroupDto;
import com.settleup.dto.group.GroupMemberDto;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.GroupMemberEntity;
import com.settleup.entity.UserEntity;
import com.settleup.enums.GroupRole;
import com.settleup.enums.MemberStatus;
import com.settleup.exception.BusinessRuleException;
import com.settleup.exception.ResourceNotFoundException;
import com.settleup.mapper.GroupMapper;
import com.settleup.repository.GroupMemberRepository;
import com.settleup.repository.GroupRepository;
import com.settleup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final BalanceService balanceService;
    private final AuditLogService auditLogService;

    @Transactional
    public GroupDto createGroup(CreateGroupRequest request, UserEntity currentUser) {
        GroupEntity group = GroupEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "USD")
                .isArchived(false)
                .build();

        group = groupRepository.save(group);

        GroupMemberEntity ownerMember = GroupMemberEntity.builder()
                .group(group)
                .user(currentUser)
                .roleInGroup(GroupRole.OWNER)
                .status(MemberStatus.ACTIVE)
                .build();

        groupMemberRepository.save(ownerMember);

        auditLogService.logChange("Group", group.getId(), "CREATE", currentUser, null, group.getName());

        return getGroupById(group.getId());
    }

    @Transactional(readOnly = true)
    public GroupDto getGroupById(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(groupId);

        return mapGroupToDto(group, members);
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getUserGroups(Long userId) {
        List<GroupEntity> groups = groupRepository.findAllByMemberUserId(userId);
        return groups.stream()
                .map(g -> getGroupById(g.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(g -> getGroupById(g.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all non-archived groups for any authenticated user to browse.
     */
    @Transactional(readOnly = true)
    public List<GroupDto> browseAllGroups() {
        return groupRepository.findByIsArchivedFalse().stream()
                .map(g -> getGroupById(g.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Allows an authenticated user to self-join a group.
     */
    @Transactional
    public GroupDto joinGroup(Long groupId, UserEntity currentUser) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (group.getIsArchived()) {
            throw new BusinessRuleException("Cannot join an archived group");
        }

        Optional<GroupMemberEntity> existingMemberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId());

        if (existingMemberOpt.isPresent()) {
            GroupMemberEntity existing = existingMemberOpt.get();
            if (existing.getStatus() == MemberStatus.ACTIVE) {
                throw new BusinessRuleException("You are already a member of this group");
            } else {
                // Re-activate member who previously left
                existing.setStatus(MemberStatus.ACTIVE);
                existing.setLeftAt(null);
                groupMemberRepository.save(existing);
            }
        } else {
            GroupMemberEntity newMember = GroupMemberEntity.builder()
                    .group(group)
                    .user(currentUser)
                    .roleInGroup(GroupRole.MEMBER)
                    .status(MemberStatus.ACTIVE)
                    .build();
            groupMemberRepository.save(newMember);
        }

        auditLogService.logChange("GroupMember", group.getId(), "JOIN_GROUP", currentUser, null, currentUser.getEmail());

        return getGroupById(groupId);
    }

    @Transactional
    public GroupDto addMemberByEmail(Long groupId, AddMemberRequest request, UserEntity currentUser) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (group.getIsArchived()) {
            throw new BusinessRuleException("Cannot add members to an archived group");
        }

        UserEntity userToAdd = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        Optional<GroupMemberEntity> existingMemberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, userToAdd.getId());

        if (existingMemberOpt.isPresent()) {
            GroupMemberEntity existing = existingMemberOpt.get();
            if (existing.getStatus() == MemberStatus.ACTIVE) {
                throw new BusinessRuleException("User " + userToAdd.getEmail() + " is already an active member of this group");
            } else {
                // Re-activate member who previously left
                existing.setStatus(MemberStatus.ACTIVE);
                existing.setLeftAt(null);
                groupMemberRepository.save(existing);
            }
        } else {
            GroupMemberEntity newMember = GroupMemberEntity.builder()
                    .group(group)
                    .user(userToAdd)
                    .roleInGroup(GroupRole.MEMBER)
                    .status(MemberStatus.ACTIVE)
                    .build();
            groupMemberRepository.save(newMember);
        }

        auditLogService.logChange("GroupMember", group.getId(), "ADD_MEMBER", currentUser, null, userToAdd.getEmail());

        return getGroupById(groupId);
    }

    @Transactional
    public GroupDto removeMember(Long groupId, Long targetUserId, UserEntity currentUser) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        GroupMemberEntity member = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in group"));

        if (member.getStatus() == MemberStatus.LEFT) {
            throw new BusinessRuleException("Member has already left the group");
        }

        // Check net balance
        BigDecimal netBalance = balanceService.getUserNetBalanceInGroup(groupId, targetUserId);
        if (netBalance.compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new BusinessRuleException("Member " + member.getUser().getName() +
                    " cannot leave/be removed with a non-zero balance of " + netBalance + " " + group.getCurrency() + ". Settle up first.");
        }

        // Soft remove
        member.setStatus(MemberStatus.LEFT);
        member.setLeftAt(LocalDateTime.now());
        groupMemberRepository.save(member);

        auditLogService.logChange("GroupMember", group.getId(), "REMOVE_MEMBER", currentUser, member.getUser().getEmail(), "LEFT");

        return getGroupById(groupId);
    }

    @Transactional
    public GroupDto archiveGroup(Long groupId, UserEntity currentUser) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        Boolean oldArchived = group.getIsArchived();
        group.setIsArchived(true);
        group = groupRepository.save(group);

        auditLogService.logChange("Group", group.getId(), "ARCHIVE", currentUser, "isArchived: " + oldArchived, "isArchived: true");

        return getGroupById(groupId);
    }

    private GroupDto mapGroupToDto(GroupEntity group, List<GroupMemberEntity> members) {
        GroupDto dto = groupMapper.toDtoWithoutMembers(group);
        dto.setMembers(groupMapper.toMemberDtoList(members));
        return dto;
    }
}
