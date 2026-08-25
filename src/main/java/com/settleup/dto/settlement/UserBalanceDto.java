package com.settleup.dto.settlement;

import com.settleup.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Net balance for a user in a group")
public class UserBalanceDto {

    private UserDto user;

    @Schema(example = "40.00", description = "Positive value means user is owed money; negative value means user owes money.")
    private BigDecimal netBalance;

    @Schema(example = "120.00", description = "Total paid for expenses by this user.")
    private BigDecimal totalPaid;

    @Schema(example = "80.00", description = "Total owed for expenses by this user.")
    private BigDecimal totalOwed;

    @Schema(example = "0.00", description = "Total confirmed settlements sent.")
    private BigDecimal settlementsSent;

    @Schema(example = "0.00", description = "Total confirmed settlements received.")
    private BigDecimal settlementsReceived;
}
