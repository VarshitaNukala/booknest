package com.booknest.service;


import com.booknest.dto.response.AuthResponse;
import com.booknest.entity.User;
import com.booknest.exception.BusinessRuleException;
import com.booknest.exception.UnauthorizedException;
import com.booknest.repository.UserRepository;
import com.booknest.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;



    public AuthResponse refreshToken(String refreshToken) {
        // Extract email from the refresh token
        String userEmail;
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (userEmail == null) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // Find the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        // Validate the refresh token
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("Refresh token has expired or is invalid");
        }

        // Generate new access token and new refresh token (rotation)
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000)  // 24 hours in milliseconds
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}