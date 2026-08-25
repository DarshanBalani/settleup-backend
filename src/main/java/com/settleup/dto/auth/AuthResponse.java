package com.settleup.dto.auth;

import com.settleup.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response containing JWT tokens")
public class AuthResponse {

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(example = "d87a4192-3c4f-4d92...")
    private String refreshToken;

    @Builder.Default
    @Schema(example = "Bearer")
    private String tokenType = "Bearer";

    private UserDto user;
}
