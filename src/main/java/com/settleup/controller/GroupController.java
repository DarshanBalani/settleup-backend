package com.settleup.controller;

import com.settleup.dto.common.ApiResponseDto;
import com.settleup.dto.group.AddMemberRequest;
import com.settleup.dto.group.CreateGroupRequest;
import com.settleup.dto.group.GroupDto;
import com.settleup.security.CustomUserDetails;
import com.settleup.service.GroupService;
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
@Tag(name = "Group Management", description = "Endpoints for creating and managing expense groups and group memberships")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/api/groups")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new group", description = "The creator becomes the group OWNER.")
    public ResponseEntity<ApiResponseDto<GroupDto>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupDto group = groupService.createGroup(request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(group, "Group created successfully"));
    }

    @GetMapping("/api/groups")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get groups the current user belongs to")
    public ResponseEntity<ApiResponseDto<List<GroupDto>>> getUserGroups(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GroupDto> groups = groupService.getUserGroups(userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.success(groups));
    }

    @GetMapping("/api/admin/groups")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all groups system-wide (Admin Only)")
    public ResponseEntity<ApiResponseDto<List<GroupDto>>> getAllGroups() {
        List<GroupDto> groups = groupService.getAllGroups();
        return ResponseEntity.ok(ApiResponseDto.success(groups));
    }

    @GetMapping("/api/groups/browse")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Browse all available groups", description = "Returns all non-archived groups for users to discover and join.")
    public ResponseEntity<ApiResponseDto<List<GroupDto>>> browseGroups() {
        List<GroupDto> groups = groupService.browseAllGroups();
        return ResponseEntity.ok(ApiResponseDto.success(groups));
    }

    @PostMapping("/api/groups/{id}/join")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Join a group", description = "Allows authenticated user to self-join a non-archived group.")
    public ResponseEntity<ApiResponseDto<GroupDto>> joinGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupDto group = groupService.joinGroup(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(group, "Joined group successfully"));
    }

    @GetMapping("/api/groups/{id}")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Get group details by ID", description = "Requires group membership or system admin role.")
    public ResponseEntity<ApiResponseDto<GroupDto>> getGroupById(@PathVariable Long id) {
        GroupDto group = groupService.getGroupById(id);
        return ResponseEntity.ok(ApiResponseDto.success(group));
    }

    @PostMapping("/api/groups/{id}/members")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Add member to group by email", description = "Adds registered user to group as MEMBER.")
    public ResponseEntity<ApiResponseDto<GroupDto>> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupDto updated = groupService.addMemberByEmail(id, request, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Member added successfully"));
    }

    @DeleteMapping("/api/groups/{id}/members/{userId}")
    @PreAuthorize("@groupSecurity.isMember(#id, authentication)")
    @Operation(summary = "Remove member from group or leave group", description = "Soft-deletes membership (sets status to LEFT). Blocked if member has non-zero balance.")
    public ResponseEntity<ApiResponseDto<GroupDto>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupDto updated = groupService.removeMember(id, userId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Member removed successfully"));
    }

    @PutMapping("/api/groups/{id}/archive")
    @PreAuthorize("@groupSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Archive a group", description = "Group owner or system admin can archive a group.")
    public ResponseEntity<ApiResponseDto<GroupDto>> archiveGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupDto updated = groupService.archiveGroup(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Group archived successfully"));
    }
}
