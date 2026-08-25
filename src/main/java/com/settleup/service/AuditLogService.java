package com.settleup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.settleup.dto.audit.AuditLogDto;
import com.settleup.entity.AuditLogEntity;
import com.settleup.entity.UserEntity;
import com.settleup.mapper.AuditLogMapper;
import com.settleup.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogMapper auditLogMapper;

    @Transactional
    public void logChange(String entityType, Long entityId, String action, UserEntity performedBy, Object oldValue, Object newValue) {
        try {
            String oldJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
            String newJson = newValue != null ? objectMapper.writeValueAsString(newValue) : null;

            AuditLogEntity logEntity = AuditLogEntity.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .performedBy(performedBy)
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .build();

            auditLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Failed to create audit log entry for entityType={}, entityId={}", entityType, entityId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(auditLogMapper::toDto);
    }

    public AuditLogDto mapToDto(AuditLogEntity entity) {
        return auditLogMapper.toDto(entity);
    }
}
