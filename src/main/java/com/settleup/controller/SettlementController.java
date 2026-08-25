package com.settleup.controller;

import com.settleup.dto.common.ApiResponseDto;
import com.settleup.dto.settlement.CreateSettlementRequest;
import com.settleup.dto.settlement.GroupBalancesResponse;
import com.settleup.dto.settlement.SettlementDto;
import com.settleup.dto.settlement.SettlementPlanDto;
import com.settleup.security.CustomUserDetails;
import com.settleup.service.BalanceService;
import com.settleup.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Balances & Settlements", description = "Endpoints for net group balances, greedy minimum transaction settlement plans, and recording/confirming debt payments")
public class SettlementController {

    private final BalanceService balanceService;
    private final SettlementService settlementService;

    @GetMapping("/api/groups/{id}/balances")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Get current net balance for each group member", description = "Dynamic computation: sum(paid) - sum(owed) + settlementsSent - settlementsReceived.")
    public ResponseEntity<ApiResponseDto<GroupBalancesResponse>> getGroupBalances(@PathVariable Long id) {
        GroupBalancesResponse response = balanceService.getGroupBalances(id);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/api/groups/{id}/settlement-plan")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Get minimum transaction settlement payoff plan", description = "Computes heuristic minimum-transaction payoff plan matching max creditors with max debtors.")
    public ResponseEntity<ApiResponseDto<List<SettlementPlanDto>>> getSettlementPlan(@PathVariable Long id) {
        List<SettlementPlanDto> plan = settlementService.computeSettlementPlan(id);
        return ResponseEntity.ok(ApiResponseDto.success(plan));
    }

    @GetMapping("/api/groups/{id}/settlements")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Get settlement transactions history for a group")
    public ResponseEntity<ApiResponseDto<List<SettlementDto>>> getGroupSettlements(@PathVariable Long id) {
        List<SettlementDto> settlements = settlementService.getGroupSettlements(id);
        return ResponseEntity.ok(ApiResponseDto.success(settlements));
    }

    @PostMapping("/api/groups/{id}/settlements")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Record a settlement transaction", description = "Creates a settlement in PENDING status. Idempotent check guards against duplicate submissions.")
    public ResponseEntity<ApiResponseDto<SettlementDto>> createSettlement(
            @PathVariable Long id,
            @Valid @RequestBody CreateSettlementRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SettlementDto settlement = settlementService.createSettlement(id, request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(settlement, "Settlement recorded in PENDING status"));
    }

    @PutMapping("/api/settlements/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirm receipt of settlement payment", description = "Only the receiver (paidTo user) can confirm a settlement. Updates status to CONFIRMED and adjusts balances.")
    public ResponseEntity<ApiResponseDto<SettlementDto>> confirmSettlement(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SettlementDto confirmed = settlementService.confirmSettlement(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(confirmed, "Settlement confirmed successfully"));
    }
}
