package com.pesira.backend.mapper;

import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.entity.User;
import com.pesira.backend.security.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserResponse toResponse(UserPrincipal principal) {
        return UserResponse.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .role(principal.getRole())
                .build();
    }
}
