package com.settleup.mapper;

import com.settleup.dto.user.UserDto;
import com.settleup.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-25T17:09:31+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.createdAt( user.getCreatedAt() );
        userDto.email( user.getEmail() );
        userDto.id( user.getId() );
        userDto.isActive( user.getIsActive() );
        userDto.name( user.getName() );
        userDto.role( user.getRole() );

        return userDto.build();
    }

    @Override
    public UserEntity toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.createdAt( userDto.getCreatedAt() );
        userEntity.email( userDto.getEmail() );
        userEntity.id( userDto.getId() );
        userEntity.isActive( userDto.getIsActive() );
        userEntity.name( userDto.getName() );
        userEntity.role( userDto.getRole() );

        return userEntity.build();
    }
}
