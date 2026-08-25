package com.settleup.mapper;

import com.settleup.dto.settlement.SettlementDto;
import com.settleup.entity.SettlementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SettlementMapper {

    @Mapping(target = "groupId", source = "group.id")
    SettlementDto toDto(SettlementEntity settlement);
}
