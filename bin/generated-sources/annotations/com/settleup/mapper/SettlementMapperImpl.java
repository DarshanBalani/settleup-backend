package com.settleup.mapper;

import com.settleup.dto.settlement.SettlementDto;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.SettlementEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-25T17:09:29+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class SettlementMapperImpl implements SettlementMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public SettlementDto toDto(SettlementEntity settlement) {
        if ( settlement == null ) {
            return null;
        }

        SettlementDto.SettlementDtoBuilder settlementDto = SettlementDto.builder();

        settlementDto.groupId( settlementGroupId( settlement ) );
        settlementDto.amount( settlement.getAmount() );
        settlementDto.createdAt( settlement.getCreatedAt() );
        settlementDto.date( settlement.getDate() );
        settlementDto.id( settlement.getId() );
        settlementDto.note( settlement.getNote() );
        settlementDto.paidBy( userMapper.toDto( settlement.getPaidBy() ) );
        settlementDto.paidTo( userMapper.toDto( settlement.getPaidTo() ) );
        settlementDto.status( settlement.getStatus() );

        return settlementDto.build();
    }

    private Long settlementGroupId(SettlementEntity settlementEntity) {
        if ( settlementEntity == null ) {
            return null;
        }
        GroupEntity group = settlementEntity.getGroup();
        if ( group == null ) {
            return null;
        }
        Long id = group.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
