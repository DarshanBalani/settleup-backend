package com.settleup.mapper;

import com.settleup.dto.audit.AuditLogDto;
import com.settleup.entity.AuditLogEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-25T17:09:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class AuditLogMapperImpl implements AuditLogMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public AuditLogDto toDto(AuditLogEntity auditLog) {
        if ( auditLog == null ) {
            return null;
        }

        AuditLogDto.AuditLogDtoBuilder auditLogDto = AuditLogDto.builder();

        auditLogDto.action( auditLog.getAction() );
        auditLogDto.entityId( auditLog.getEntityId() );
        auditLogDto.entityType( auditLog.getEntityType() );
        auditLogDto.id( auditLog.getId() );
        auditLogDto.newValue( auditLog.getNewValue() );
        auditLogDto.oldValue( auditLog.getOldValue() );
        auditLogDto.performedBy( userMapper.toDto( auditLog.getPerformedBy() ) );
        auditLogDto.timestamp( auditLog.getTimestamp() );

        return auditLogDto.build();
    }
}
