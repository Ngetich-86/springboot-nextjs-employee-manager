package com.pesira.backend.service;

import com.pesira.backend.dto.auth.AuthResponse;
import com.pesira.backend.dto.auth.LoginRequest;
import com.pesira.backend.dto.auth.RegisterRequest;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.security.UserPrincipal;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    UserResponse getCurrentUser(UserPrincipal principal);
}
