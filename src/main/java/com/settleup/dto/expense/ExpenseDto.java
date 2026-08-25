package com.settleup.dto.expense;

import com.settleup.dto.user.UserDto;
import com.settleup.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Expense representation")
public class ExpenseDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long groupId;

    @Schema(example = "Dinner at Italian Restaurant")
    private String description;

    @Schema(example = "120.00")
    private BigDecimal totalAmount;

    private UserDto paidBy;

    @Schema(example = "FOOD")
    private ExpenseCategory category;

    private LocalDateTime date;

    private UserDto createdBy;

    @Schema(example = "false")
    private Boolean isDeleted;

    @Schema(example = "Shared pasta and wine")
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ExpenseSplitDto> splits;
}
