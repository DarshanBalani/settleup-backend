package com.settleup.controller;

import com.settleup.dto.common.ApiResponseDto;
import com.settleup.dto.expense.CreateExpenseRequest;
import com.settleup.dto.expense.ExpenseDto;
import com.settleup.dto.expense.UpdateExpenseRequest;
import com.settleup.enums.ExpenseCategory;
import com.settleup.security.CustomUserDetails;
import com.settleup.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "Endpoints for creating, updating, deleting, and searching group expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/api/groups/{id}/expenses")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Create an expense in a group", description = "Supports EQUAL, EXACT, PERCENTAGE, and SHARES splits.")
    public ResponseEntity<ApiResponseDto<ExpenseDto>> createExpense(
            @PathVariable Long id,
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ExpenseDto expense = expenseService.createExpense(id, request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(expense, "Expense created successfully"));
    }

    @GetMapping("/api/groups/{id}/expenses")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Get paginated, filterable group expenses", description = "Filter by date range, category, or payer.")
    public ResponseEntity<ApiResponseDto<Page<ExpenseDto>>> getGroupExpenses(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) Long paidById,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExpenseDto> result = expenseService.getGroupExpenses(id, startDate, endDate, category, paidById, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    @PutMapping("/api/expenses/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update an existing expense", description = "Recomputes splits and writes AuditLog entry.")
    public ResponseEntity<ApiResponseDto<ExpenseDto>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ExpenseDto updated = expenseService.updateExpense(id, request, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Expense updated successfully"));
    }

    @DeleteMapping("/api/expenses/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Soft delete an expense", description = "Marks expense as deleted, recomputes balances, and writes AuditLog entry.")
    public ResponseEntity<ApiResponseDto<Void>> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        expenseService.deleteExpense(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(null, "Expense deleted successfully"));
    }
}
