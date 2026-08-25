package com.settleup.repository;

import com.settleup.entity.GroupMemberEntity;
import com.settleup.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
    
    Optional<GroupMemberEntity> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMemberEntity> findByGroupId(Long groupId);

    List<GroupMemberEntity> findByGroupIdAndStatus(Long groupId, MemberStatus status);

    boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, MemberStatus status);
}
