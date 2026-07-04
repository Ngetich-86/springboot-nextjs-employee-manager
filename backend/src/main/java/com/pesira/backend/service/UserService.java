package com.pesira.backend.service;

import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.dto.user.UpdateUserRoleRequest;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserResponse> findAll(String search, Pageable pageable);

    UserResponse updateRole(Long id, UpdateUserRoleRequest request);
}
