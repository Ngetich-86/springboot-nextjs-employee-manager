package com.pesira.backend.dto.auth;

import com.pesira.backend.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String email;
    private final Role role;
}
