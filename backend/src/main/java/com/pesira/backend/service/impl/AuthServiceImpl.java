package com.pesira.backend.service.impl;

import com.pesira.backend.config.JwtProperties;
import com.pesira.backend.dto.auth.AuthResponse;
import com.pesira.backend.dto.auth.LoginRequest;
import com.pesira.backend.dto.auth.RegisterRequest;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.entity.User;
import com.pesira.backend.enums.Role;
import com.pesira.backend.exception.BusinessException;
import com.pesira.backend.mapper.UserMapper;
import com.pesira.backend.repository.UserRepository;
import com.pesira.backend.security.JwtService;
import com.pesira.backend.security.UserPrincipal;
import com.pesira.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return buildAuthResponse(principal);
        } catch (BadCredentialsException exception) {
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered", HttpStatus.CONFLICT.value());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(savedUser);
        return buildAuthResponse(principal);
    }

    @Override
    public UserResponse getCurrentUser(UserPrincipal principal) {
        return userMapper.toResponse(principal);
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal) {
        String accessToken = jwtService.generateToken(principal);
        UserResponse user = userMapper.toResponse(principal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(jwtProperties.getExpirationMs() / 1000)
                .user(user)
                .build();
    }
}
