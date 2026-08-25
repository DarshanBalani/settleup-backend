package com.settleup.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Simple user summary representation")
public class UserSummaryDto {

    @Schema(example = "1")
    private Long userId;

    @Schema(example = "Alice Smith")
    private String name;
}
