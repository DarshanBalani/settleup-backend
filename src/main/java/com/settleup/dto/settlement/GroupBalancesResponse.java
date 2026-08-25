package com.settleup.dto.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Group net balances response")
public class GroupBalancesResponse {

    @Schema(example = "1")
    private Long groupId;

    @Schema(example = "USD")
    private String currency;

    private List<UserBalanceDto> balances;
}
