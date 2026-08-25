package com.settleup.dto.expense;

import com.settleup.enums.ExpenseCategory;
import com.settleup.enums.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Update expense request payload")
public class UpdateExpenseRequest {

    @NotBlank(message = "Description is required")
    @Schema(example = "Updated Dinner at Italian Restaurant")
    private String description;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    @Schema(example = "150.00")
    private BigDecimal totalAmount;

    @NotNull(message = "PaidBy user ID is required")
    @Schema(example = "1")
    private Long paidById;

    @NotNull(message = "Category is required")
    @Schema(example = "FOOD")
    private ExpenseCategory category;

    @NotNull(message = "Split type is required")
    @Schema(example = "EQUAL")
    private SplitType splitType;

    @Schema(example = "2026-08-25T19:30:00")
    private LocalDateTime date;

    @Schema(example = "Added dessert")
    private String notes;

    @NotEmpty(message = "At least one split participant is required")
    @Valid
    private List<SplitInputDto> splits;
}
