package com.settleup.repository;

import com.settleup.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByEntityTypeAndEntityId(String entityType, Long entityId);
    Page<AuditLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);
}
