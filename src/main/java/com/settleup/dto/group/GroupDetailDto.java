package com.settleup.dto.group;

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
@Schema(description = "Group detailed representation")
public class GroupDetailDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Summer Trip 2026")
    private String name;

    @Schema(example = "Beach vacation expenses")
    private String description;

    @Schema(example = "INR")
    private String currency;

    @Schema(example = "false")
    private Boolean isArchived;

    private GroupCreatorDto createdBy;

    private LocalDateTime createdAt;

    private List<GroupMemberDetailDto> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupCreatorDto {
        @Schema(example = "1")
        private Long id;

        @Schema(example = "Alice Smith")
        private String name;
    }
}
