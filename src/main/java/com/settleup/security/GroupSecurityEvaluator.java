package com.settleup.security;

import com.settleup.enums.GroupRole;
import com.settleup.enums.MemberStatus;
import com.settleup.enums.Role;
import com.settleup.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("groupSecurity")
@RequiredArgsConstructor
public class GroupSecurityEvaluator {

    private final GroupMemberRepository groupMemberRepository;

    public boolean isMember(Long groupId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }

        // Admin has system-wide access
        if (userDetails.getUser().getRole() == Role.ADMIN) {
            return true;
        }

        return groupMemberRepository.existsByGroupIdAndUserIdAndStatus(
                groupId, userDetails.getId(), MemberStatus.ACTIVE);
    }

    public boolean isOwner(Long groupId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }

        // Admin has system-wide access
        if (userDetails.getUser().getRole() == Role.ADMIN) {
            return true;
        }

        return groupMemberRepository.findByGroupIdAndUserId(groupId, userDetails.getId())
                .map(gm -> gm.getRoleInGroup() == GroupRole.OWNER && gm.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }
}
