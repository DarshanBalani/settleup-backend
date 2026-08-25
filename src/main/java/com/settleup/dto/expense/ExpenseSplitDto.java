package com.settleup.dto.expense;

import com.settleup.dto.user.UserDto;
import com.settleup.enums.SplitType;
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
@Schema(description = "Expense split detail representation")
public class ExpenseSplitDto {

    @Schema(example = "1")
    private Long id;

    private UserDto user;

    @Schema(example = "EQUAL")
    private SplitType splitType;

    @Schema(example = "40.00")
    private BigDecimal amountOwed;

    @Schema(example = "33.33")
    private BigDecimal splitValue;
}
