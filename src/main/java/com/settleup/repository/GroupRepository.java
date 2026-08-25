package com.settleup.repository;

import com.settleup.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    
    @Query("SELECT gm.group FROM GroupMemberEntity gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE'")
    List<GroupEntity> findAllByMemberUserId(@Param("userId") Long userId);

    List<GroupEntity> findByIsArchivedFalse();
}
