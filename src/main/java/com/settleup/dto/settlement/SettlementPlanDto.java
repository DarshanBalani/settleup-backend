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
@Schema(description = "Suggested transfer item in minimum settlement plan")
public class SettlementPlanDto {

    private UserDto fromUser;
    private UserDto toUser;

    @Schema(example = "40.00")
    private BigDecimal amount;
}
