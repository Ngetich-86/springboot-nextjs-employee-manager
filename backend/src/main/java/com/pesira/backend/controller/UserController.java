package com.pesira.backend.controller;

import com.pesira.backend.dto.ApiResponse;
import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.dto.user.UpdateUserRoleRequest;
import com.pesira.backend.service.UserService;
import com.pesira.backend.util.ApiConstants;
import com.pesira.backend.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.USERS_API)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management", description = "Administrative user management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List and search users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        PageResponse<UserResponse> users = userService.findAll(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Update a user's role")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        UserResponse user = userService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", user));
    }
}
