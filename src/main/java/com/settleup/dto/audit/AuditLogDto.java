package com.settleup.dto.audit;

import com.settleup.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Audit log record payload")
public class AuditLogDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Expense")
    private String entityType;

    @Schema(example = "10")
    private Long entityId;

    @Schema(example = "UPDATE")
    private String action;

    private UserDto performedBy;

    private LocalDateTime timestamp;

    private String oldValue;
    private String newValue;
}
