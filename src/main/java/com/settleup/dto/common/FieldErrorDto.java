package com.settleup.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Validation field error detail")
public class FieldErrorDto {

    @Schema(example = "totalAmount")
    private String field;

    @Schema(example = "Total amount must be greater than zero")
    private String message;
}
