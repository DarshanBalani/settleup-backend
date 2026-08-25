package com.settleup.repository;

import com.settleup.entity.SettlementEntity;
import com.settleup.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    List<SettlementEntity> findByGroupId(Long groupId);
    List<SettlementEntity> findByGroupIdAndStatus(Long groupId, SettlementStatus status);
}
