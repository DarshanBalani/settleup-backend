package com.settleup.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Split definition for a single user in an expense")
public class SplitInputDto {

    @NotNull(message = "User ID is required")
    @Schema(example = "1")
    private Long userId;

    @Schema(example = "50.00", description = "Exact amount for EXACT split, percentage for PERCENTAGE split, shares count for SHARES split. Ignored for EQUAL split.")
    private BigDecimal splitValue;
}
