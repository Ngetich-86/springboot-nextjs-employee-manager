package com.pesira.backend.security;

import com.pesira.backend.AbstractIntegrationTest;
import com.pesira.backend.config.JwtProperties;
import com.pesira.backend.entity.User;
import com.pesira.backend.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises JwtAuthenticationFilter through the real Spring Security filter
 * chain (as opposed to JwtServiceTest, which tests JwtService in isolation),
 * verifying the observable HTTP contract for valid, expired, and malformed
 * bearer tokens.
 */
class JwtAuthenticationFilterIntegrationTest extends AbstractIntegrationTest {

    private static final String PROTECTED_ENDPOINT = "/api/v1/auth/me";

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.jwt.secret}")
    private String configuredSecret;

    @Test
    void requestWithExpiredTokenReturnsControlled401() throws Exception {
        String expiredToken = generateToken(-1000L);

        mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void requestWithMalformedTokenReturnsControlled401() throws Exception {
        mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header("Authorization", "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void requestWithTokenSignedByAnUnknownKeyReturnsControlled401() throws Exception {
        JwtProperties otherKeyProperties = new JwtProperties();
        otherKeyProperties.setSecret("a-completely-different-secret-key-with-at-least-256-bits-of-length");
        otherKeyProperties.setExpirationMs(3600000L);
        JwtService otherKeyService = new JwtService(otherKeyProperties);

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        String tokenWithBadSignature = otherKeyService.generateToken(new UserPrincipal(user));

        mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header("Authorization", "Bearer " + tokenWithBadSignature))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void blankBearerTokenCurrentlyEscapesTheFilterChainUncaught() {
        assertThatThrownBy(() -> mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header("Authorization", "Bearer ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whitespaceOnlyBearerTokenCurrentlyEscapesTheFilterChainUncaught() {
        assertThatThrownBy(() -> mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header("Authorization", "Bearer    ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requestWithoutTokenReturnsControlled401ForProtectedEndpoint() throws Exception {
        mockMvc.perform(get(PROTECTED_ENDPOINT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void requestWithoutTokenIsStillAllowedForAnonymousEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithValidTokenAuthenticatesSuccessfully() throws Exception {
        String validToken = generateToken(3600000L);

        MvcResult result = mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@example.com"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotBlank();
    }

    private String generateToken(long expirationMs) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(configuredSecret);
        properties.setExpirationMs(expirationMs);
        JwtService jwtService = new JwtService(properties);

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        return jwtService.generateToken(new UserPrincipal(user));
    }
}
