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
import com.pesira.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PageMapper pageMapper;

    @Override
    public PageResponse<UserResponse> findAll(String search, Pageable pageable) {
        Page<User> page = userRepository.searchUsers(normalizeSearch(search), pageable);
        return pageMapper.toUserPageResponse(page, userMapper);
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role currentRole = user.getRole();
        Role newRole = request.getRole();

        if (currentRole == newRole) {
            return userMapper.toResponse(user);
        }

        if (currentRole == Role.ADMIN && newRole == Role.USER) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new BusinessException(
                        "Cannot demote the last remaining administrator",
                        HttpStatus.CONFLICT.value());
            }
        }

        user.setRole(newRole);
        return userMapper.toResponse(user);
    }

    private String normalizeSearch(String search) {
        return search == null ? "" : search.trim();
    }
}
