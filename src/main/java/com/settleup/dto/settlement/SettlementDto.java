package com.settleup.dto.settlement;

import com.settleup.dto.user.UserDto;
import com.settleup.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Settlement record details")
public class SettlementDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long groupId;

    private UserDto paidBy;

    private UserDto paidTo;

    @Schema(example = "50.00")
    private BigDecimal amount;

    @Schema(example = "Settling dinner debt via UPI/Venmo")
    private String note;

    @Schema(example = "PENDING")
    private SettlementStatus status;

    private LocalDateTime date;

    private LocalDateTime createdAt;
}
