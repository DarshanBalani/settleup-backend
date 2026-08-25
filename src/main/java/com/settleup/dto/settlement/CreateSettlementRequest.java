package com.settleup.dto.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "Create settlement transaction payload")
public class CreateSettlementRequest {

    @NotNull(message = "PaidTo user ID is required")
    @Schema(example = "2")
    private Long paidToId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(example = "50.00")
    private BigDecimal amount;

    @Schema(example = "Settling dinner debt via UPI/Venmo")
    private String note;
}
