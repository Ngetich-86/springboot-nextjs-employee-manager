package com.pesira.backend.service.impl;

import com.pesira.backend.config.JwtProperties;
import com.pesira.backend.dto.auth.AuthResponse;
import com.pesira.backend.dto.auth.LoginRequest;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.enums.Role;
import com.pesira.backend.exception.BusinessException;
import com.pesira.backend.mapper.UserMapper;
import com.pesira.backend.security.JwtService;
import com.pesira.backend.security.UserPrincipal;
import com.pesira.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserPrincipal userPrincipal;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);

        userPrincipal = new UserPrincipal(user);
        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("admin123");
    }

    @Test
    void loginReturnsAuthResponseWhenCredentialsAreValid() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null);
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userPrincipal)).thenReturn("jwt-token");
        when(jwtProperties.getExpirationMs()).thenReturn(86400000L);
        when(userMapper.toResponse(userPrincipal)).thenReturn(userResponse);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUser()).isEqualTo(userResponse);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginThrowsBusinessExceptionWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getMessage()).isEqualTo("Invalid email or password");
                    assertThat(businessException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                });
    }

    @Test
    void getCurrentUserReturnsMappedUserResponse() {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(userMapper.toResponse(userPrincipal)).thenReturn(userResponse);

        UserResponse response = authService.getCurrentUser(userPrincipal);

        assertThat(response).isEqualTo(userResponse);
    }
}
