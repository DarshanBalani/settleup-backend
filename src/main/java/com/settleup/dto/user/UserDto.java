package com.settleup.dto.user;

import com.settleup.enums.Role;
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
@Schema(description = "User details representation")
public class UserDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Alice Smith")
    private String name;

    @Schema(example = "alice@example.com")
    private String email;

    @Schema(example = "USER")
    private Role role;

    @Schema(example = "true")
    private Boolean isActive;

    private LocalDateTime createdAt;
}
