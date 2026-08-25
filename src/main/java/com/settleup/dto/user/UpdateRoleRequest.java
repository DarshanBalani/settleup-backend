package com.settleup.dto.user;

import com.settleup.enums.Role;
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
@Schema(description = "Admin payload to update user role")
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(example = "ADMIN")
    private Role role;
}
