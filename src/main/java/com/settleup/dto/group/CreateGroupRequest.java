package com.settleup.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Group creation payload")
public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 100, message = "Group name must be between 2 and 100 characters")
    @Schema(example = "Summer Trip 2026")
    private String name;

    @Schema(example = "Beach vacation expenses")
    private String description;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 10, message = "Currency code must be between 3 and 10 characters")
    @Schema(example = "USD")
    @Builder.Default
    private String currency = "USD";
}
