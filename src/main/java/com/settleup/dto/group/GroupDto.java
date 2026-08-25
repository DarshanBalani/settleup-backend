package com.settleup.dto.group;

import com.settleup.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Group details representation")
public class GroupDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Summer Trip 2026")
    private String name;

    @Schema(example = "Beach vacation expenses")
    private String description;

    private UserDto createdBy;

    @Schema(example = "USD")
    private String currency;

    @Schema(example = "false")
    private Boolean isArchived;

    private LocalDateTime createdAt;

    private List<GroupMemberDto> members;
}
