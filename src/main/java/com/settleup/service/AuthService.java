package com.settleup.service;

import com.settleup.dto.auth.AuthResponse;
import com.settleup.dto.auth.LoginRequest;
import com.settleup.dto.auth.RefreshTokenRequest;
import com.settleup.dto.auth.RegisterRequest;
import com.settleup.dto.user.UserDto;
import com.settleup.entity.RefreshTokenEntity;
import com.settleup.entity.UserEntity;
import com.settleup.enums.Role;
import com.settleup.exception.BusinessRuleException;
import com.settleup.exception.ResourceNotFoundException;
import com.settleup.repository.RefreshTokenRepository;
import com.settleup.repository.UserRepository;
import com.settleup.security.CustomUserDetails;
import com.settleup.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email is already registered: " + request.getEmail());
        }

        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Self-registration can ONLY create USER role
                .isActive(true)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessTokenForUser(
                user.getEmail(), user.getId(), user.getRole().name());
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken();

        saveRefreshToken(user, rawRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .user(userService.mapToDto(user))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new BusinessRuleException("User account is deactivated. Contact an administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken();

        saveRefreshToken(userDetails.getUser(), rawRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .user(userService.mapToDto(userDetails.getUser()))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("Invalid or revoked refresh token"));

        if (refreshTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenEntity.setRevoked(true);
            refreshTokenRepository.save(refreshTokenEntity);
            throw new BusinessRuleException("Refresh token has expired. Please login again.");
        }

        UserEntity user = refreshTokenEntity.getUser();
        if (!user.getIsActive()) {
            throw new BusinessRuleException("User account is deactivated.");
        }

        // Revoke used token
        refreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(refreshTokenEntity);

        // Generate new token pair
        String newAccessToken = jwtTokenProvider.generateAccessTokenForUser(
                user.getEmail(), user.getId(), user.getRole().name());
        String newRawRefreshToken = jwtTokenProvider.generateRefreshToken();

        saveRefreshToken(user, newRawRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .user(userService.mapToDto(user))
                .build();
    }

    private void saveRefreshToken(UserEntity user, String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(jwtTokenProvider.getRefreshTokenExpirationMs() * 1_000_000);

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenRepository.save(tokenEntity);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
