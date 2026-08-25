package com.settleup.mapper;

import com.settleup.dto.group.GroupDto;
import com.settleup.dto.group.GroupMemberDto;
import com.settleup.entity.GroupEntity;
import com.settleup.entity.GroupMemberEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-25T17:09:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class GroupMapperImpl implements GroupMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public GroupDto toDto(GroupEntity group) {
        if ( group == null ) {
            return null;
        }

        GroupDto.GroupDtoBuilder groupDto = GroupDto.builder();

        groupDto.createdAt( group.getCreatedAt() );
        groupDto.createdBy( userMapper.toDto( group.getCreatedBy() ) );
        groupDto.currency( group.getCurrency() );
        groupDto.description( group.getDescription() );
        groupDto.id( group.getId() );
        groupDto.isArchived( group.getIsArchived() );
        groupDto.name( group.getName() );

        return groupDto.build();
    }

    @Override
    public GroupDto toDtoWithoutMembers(GroupEntity group) {
        if ( group == null ) {
            return null;
        }

        GroupDto.GroupDtoBuilder groupDto = GroupDto.builder();

        groupDto.createdAt( group.getCreatedAt() );
        groupDto.createdBy( userMapper.toDto( group.getCreatedBy() ) );
        groupDto.currency( group.getCurrency() );
        groupDto.description( group.getDescription() );
        groupDto.id( group.getId() );
        groupDto.isArchived( group.getIsArchived() );
        groupDto.name( group.getName() );

        return groupDto.build();
    }

    @Override
    public GroupMemberDto toMemberDto(GroupMemberEntity member) {
        if ( member == null ) {
            return null;
        }

        GroupMemberDto.GroupMemberDtoBuilder groupMemberDto = GroupMemberDto.builder();

        groupMemberDto.id( member.getId() );
        groupMemberDto.joinedAt( member.getJoinedAt() );
        groupMemberDto.leftAt( member.getLeftAt() );
        groupMemberDto.roleInGroup( member.getRoleInGroup() );
        groupMemberDto.status( member.getStatus() );
        groupMemberDto.user( userMapper.toDto( member.getUser() ) );

        return groupMemberDto.build();
    }

    @Override
    public List<GroupMemberDto> toMemberDtoList(List<GroupMemberEntity> members) {
        if ( members == null ) {
            return null;
        }

        List<GroupMemberDto> list = new ArrayList<GroupMemberDto>( members.size() );
        for ( GroupMemberEntity groupMemberEntity : members ) {
            list.add( toMemberDto( groupMemberEntity ) );
        }

        return list;
    }
}
