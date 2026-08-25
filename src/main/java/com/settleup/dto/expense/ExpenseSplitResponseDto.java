package com.settleup.dto.expense;

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
@Schema(description = "Expense split item")
public class ExpenseSplitResponseDto {

    @Schema(example = "1")
    private Long userId;

    @Schema(example = "Alice Smith")
    private String name;

    @Schema(example = "EQUAL")
    private SplitType splitType;

    @Schema(example = "400.00")
    private BigDecimal amountOwed;
}
