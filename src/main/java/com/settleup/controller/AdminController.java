package com.settleup.controller;

import com.settleup.dto.audit.AuditLogDto;
import com.settleup.dto.common.ApiResponseDto;
import com.settleup.dto.expense.ExpenseDto;
import com.settleup.service.AuditLogService;
import com.settleup.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Operations", description = "System-wide admin monitoring endpoints")
public class AdminController {

    private final ExpenseService expenseService;
    private final AuditLogService auditLogService;

    @GetMapping("/expenses")
    @Operation(summary = "Get all expenses system-wide (Admin Only)")
    public ResponseEntity<ApiResponseDto<Page<ExpenseDto>>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExpenseDto> result = expenseService.getAllExpenses(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    @GetMapping("/audit-log")
    @Operation(summary = "Get system-wide audit logs (Admin Only)", description = "Captures entity modifications, role changes, and admin actions.")
    public ResponseEntity<ApiResponseDto<Page<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDto> result = auditLogService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }
}
