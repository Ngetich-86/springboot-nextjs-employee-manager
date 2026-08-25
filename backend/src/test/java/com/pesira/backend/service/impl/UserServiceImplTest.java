package com.pesira.backend.service.impl;

import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.dto.user.UpdateUserRoleRequest;
import com.pesira.backend.entity.User;
import com.pesira.backend.enums.Role;
import com.pesira.backend.exception.BusinessException;
import com.pesira.backend.exception.ResourceNotFoundException;
import com.pesira.backend.mapper.PageMapper;
import com.pesira.backend.mapper.UserMapper;
import com.pesira.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User adminUser;
    private UpdateUserRoleRequest request;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        request = new UpdateUserRoleRequest();
    }

    @Test
    void updateRoleThrowsResourceNotFoundExceptionWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        request.setRole(Role.USER);

        assertThatThrownBy(() -> userService.updateRole(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateRoleIsNoOpWhenRequestedRoleMatchesCurrentRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();
        when(userMapper.toResponse(adminUser)).thenReturn(expectedResponse);
        request.setRole(Role.ADMIN);

        UserResponse response = userService.updateRole(1L, request);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(adminUser.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository, never()).countByRole(any());
    }

    @Test
    void updateRolePromotesUserToAdmin() {
        User plainUser = new User();
        plainUser.setId(2L);
        plainUser.setEmail("user@example.com");
        plainUser.setRole(Role.USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(plainUser));
        UserResponse expectedResponse = UserResponse.builder()
                .id(2L)
                .email("user@example.com")
                .role(Role.ADMIN)
                .build();
        when(userMapper.toResponse(plainUser)).thenReturn(expectedResponse);
        request.setRole(Role.ADMIN);

        UserResponse response = userService.updateRole(2L, request);

        assertThat(plainUser.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response).isEqualTo(expectedResponse);
        verify(userRepository, never()).countByRole(any());
    }

    @Test
    void updateRoleDemotesAdminWhenAnotherAdminRemains() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);
        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .email("admin@example.com")
                .role(Role.USER)
                .build();
        when(userMapper.toResponse(adminUser)).thenReturn(expectedResponse);
        request.setRole(Role.USER);

        UserResponse response = userService.updateRole(1L, request);

        assertThat(adminUser.getRole()).isEqualTo(Role.USER);
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void updateRoleThrowsConflictWhenDemotingLastRemainingAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        request.setRole(Role.USER);

        assertThatThrownBy(() -> userService.updateRole(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getMessage())
                            .isEqualTo("Cannot demote the last remaining administrator");
                    assertThat(businessException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT.value());
                });
        assertThat(adminUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void findAllTrimsSearchTermBeforeDelegatingToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<User> page = new PageImpl<>(List.of(adminUser), pageable, 1);
        when(userRepository.searchUsers(eq("admin"), eq(pageable))).thenReturn(page);
        PageResponse<UserResponse> expected = PageResponse.<UserResponse>builder()
                .content(List.of())
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        when(pageMapper.toUserPageResponse(page, userMapper)).thenReturn(expected);

        PageResponse<UserResponse> response = userService.findAll("  admin  ", pageable);

        assertThat(response).isEqualTo(expected);
        verify(userRepository).searchUsers(eq("admin"), eq(pageable));
    }

    @Test
    void findAllNormalizesNullSearchToEmptyString() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<User> page = new PageImpl<>(List.of(), pageable, 0);
        when(userRepository.searchUsers(eq(""), eq(pageable))).thenReturn(page);
        when(pageMapper.toUserPageResponse(page, userMapper)).thenReturn(PageResponse.<UserResponse>builder().build());

        userService.findAll(null, pageable);

        verify(userRepository).searchUsers(eq(""), eq(pageable));
    }
}
