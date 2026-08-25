package com.pesira.backend.security;

import com.pesira.backend.config.JwtProperties;
import com.pesira.backend.entity.User;
import com.pesira.backend.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-with-at-least-256-bits-for-hmac-sha256-algorithm");
        jwtProperties.setExpirationMs(3600000L);

        jwtService = new JwtService(jwtProperties);

        User user = new User();
        user.setId(42L);
        user.setEmail("admin@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);
        userPrincipal = new UserPrincipal(user);
    }

    @Test
    void generateTokenAndValidateToken() {
        String token = jwtService.generateToken(userPrincipal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@example.com");
        assertThat(jwtService.isTokenValid(token, userPrincipal)).isTrue();
    }

    @Test
    void isTokenValidRejectsExpiredToken() {
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret("test-secret-key-with-at-least-256-bits-for-hmac-sha256-algorithm");
        expiredProperties.setExpirationMs(-1000L);
        JwtService expiredTokenService = new JwtService(expiredProperties);

        String token = expiredTokenService.generateToken(userPrincipal);

        assertThatThrownBy(() -> expiredTokenService.isTokenValid(token, userPrincipal))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void isTokenInvalidForDifferentUser() {
        String token = jwtService.generateToken(userPrincipal);

        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setEmail("user@example.com");
        otherUser.setPassword("encoded-password");
        otherUser.setRole(Role.USER);

        assertThat(jwtService.isTokenValid(token, new UserPrincipal(otherUser))).isFalse();
    }
}
