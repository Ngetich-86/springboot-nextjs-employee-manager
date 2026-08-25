package com.pesira.backend.security;

import com.pesira.backend.AbstractIntegrationTest;
import com.pesira.backend.config.JwtProperties;
import com.pesira.backend.entity.User;
import com.pesira.backend.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Characterizes how an expired Bearer JWT is currently handled by the real
 * Spring Security filter chain (as opposed to JwtServiceTest, which exercises
 * JwtService in isolation).
 */
class JwtAuthenticationFilterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.jwt.secret}")
    private String configuredSecret;

    @Test
    void expiredBearerTokenCurrentlyEscapesTheFilterChainUncaught() {
        String expiredToken = generateExpiredToken();

        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + expiredToken)))
                .isInstanceOf(ExpiredJwtException.class);
    }

    private String generateExpiredToken() {
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret(configuredSecret);
        expiredProperties.setExpirationMs(-1000L);
        JwtService expiredTokenService = new JwtService(expiredProperties);

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        return expiredTokenService.generateToken(new UserPrincipal(user));
    }
}
