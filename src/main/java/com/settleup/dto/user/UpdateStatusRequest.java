package com.settleup.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin payload to update user active status")
public class UpdateStatusRequest {

    @NotNull(message = "isActive flag is required")
    @Schema(example = "false")
    private Boolean isActive;
}
