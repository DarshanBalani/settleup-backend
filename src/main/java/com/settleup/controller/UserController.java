package com.settleup.controller;

import com.settleup.dto.common.ApiResponseDto;
import com.settleup.dto.user.UpdateRoleRequest;
import com.settleup.dto.user.UpdateStatusRequest;
import com.settleup.dto.user.UpdateUserRequest;
import com.settleup.dto.user.UserDto;
import com.settleup.security.CustomUserDetails;
import com.settleup.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for personal profile and system-wide admin user management")
public class UserController {

    private final UserService userService;

    @GetMapping("/api/users/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponseDto<UserDto>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDto user = userService.getCurrentUser(userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(user));
    }

    @PutMapping("/api/users/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile name or password")
    public ResponseEntity<ApiResponseDto<UserDto>> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto updated = userService.updateCurrentUser(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Profile updated successfully"));
    }

    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users system-wide (Admin Only)")
    public ResponseEntity<ApiResponseDto<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponseDto.success(users));
    }

    @PutMapping("/api/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role (Admin Only)", description = "Changes system role (USER vs ADMIN) and writes AuditLog entry.")
    public ResponseEntity<ApiResponseDto<UserDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDto updated = userService.updateUserRole(id, request, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(updated, "User role updated successfully"));
    }

    @PutMapping("/api/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate user account (Admin Only)", description = "Soft-disables user login; historical expenses remain intact.")
    public ResponseEntity<ApiResponseDto<UserDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDto updated = userService.updateUserStatus(id, request, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(updated, "User status updated successfully"));
    }
}
