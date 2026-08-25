package com.settleup.dto.group;

import com.settleup.enums.GroupRole;
import com.settleup.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Group member detail item")
public class GroupMemberDetailDto {

    @Schema(example = "1")
    private Long userId;

    @Schema(example = "Alice Smith")
    private String name;

    @Schema(example = "alice@example.com")
    private String email;

    @Schema(example = "OWNER")
    private GroupRole roleInGroup;

    @Schema(example = "ACTIVE")
    private MemberStatus status;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;
}
