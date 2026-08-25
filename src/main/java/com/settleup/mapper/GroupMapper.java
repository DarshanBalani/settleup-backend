package com.settleup.mapper;

import com.settleup.dto.group.GroupDto;
import com.settleup.dto.group.GroupMemberDto;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.GroupMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GroupMapper {

    GroupDto toDto(GroupEntity group);

    @Mapping(target = "members", ignore = true)
    GroupDto toDtoWithoutMembers(GroupEntity group);

    GroupMemberDto toMemberDto(GroupMemberEntity member);

    List<GroupMemberDto> toMemberDtoList(List<GroupMemberEntity> members);
}
