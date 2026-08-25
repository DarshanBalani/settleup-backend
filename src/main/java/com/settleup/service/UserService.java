package com.settleup.service;

import com.settleup.dto.user.UpdateRoleRequest;
import com.settleup.dto.user.UpdateStatusRequest;
import com.settleup.dto.user.UpdateUserRequest;
import com.settleup.dto.user.UserDto;
import com.settleup.entity.UserEntity;
import com.settleup.enums.Role;
import com.settleup.exception.ResourceNotFoundException;
import com.settleup.mapper.UserMapper;
import com.settleup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UserEntity currentUser) {
        return mapToDto(currentUser);
    }

    @Transactional
    public UserDto updateCurrentUser(UserEntity currentUser, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUserRole(Long userId, UpdateRoleRequest request, UserEntity adminUser) {
        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role oldRole = targetUser.getRole();
        targetUser.setRole(request.getRole());
        targetUser = userRepository.save(targetUser);

        auditLogService.logChange("User", userId, "UPDATE_ROLE", adminUser,
                "Role: " + oldRole, "Role: " + request.getRole());

        return mapToDto(targetUser);
    }

    @Transactional
    public UserDto updateUserStatus(Long userId, UpdateStatusRequest request, UserEntity adminUser) {
        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Boolean oldStatus = targetUser.getIsActive();
        targetUser.setIsActive(request.getIsActive());
        targetUser = userRepository.save(targetUser);

        auditLogService.logChange("User", userId, "UPDATE_STATUS", adminUser,
                "isActive: " + oldStatus, "isActive: " + request.getIsActive());

        return mapToDto(targetUser);
    }

    public UserDto mapToDto(UserEntity user) {
        return userMapper.toDto(user);
    }
}
