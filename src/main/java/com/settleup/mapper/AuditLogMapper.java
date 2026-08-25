package com.settleup.mapper;

import com.settleup.dto.audit.AuditLogDto;
import com.settleup.entity.AuditLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {

    AuditLogDto toDto(AuditLogEntity auditLog);
}
