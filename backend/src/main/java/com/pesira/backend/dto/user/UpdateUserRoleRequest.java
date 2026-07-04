package com.pesira.backend.dto.user;

import com.pesira.backend.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
