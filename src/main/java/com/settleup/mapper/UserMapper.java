package com.settleup.mapper;

import com.settleup.dto.user.UserDto;
import com.settleup.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto toDto(UserEntity user);
    UserEntity toEntity(UserDto userDto);
}
