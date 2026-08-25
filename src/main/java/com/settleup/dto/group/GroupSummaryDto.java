package com.settleup.dto.group;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Schema(description = "Group summary item")
public class GroupSummaryDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Summer Trip 2026")
    private String name;

    @Schema(example = "Beach vacation expenses")
    private String description;

    @Schema(example = "INR")
    private String currency;

    @Schema(example = "false")
    private Boolean isArchived;

    @Schema(example = "4")
    private Integer memberCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(example = "-450.00")
    private BigDecimal netBalance;
}
