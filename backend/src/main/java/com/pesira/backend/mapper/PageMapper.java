package com.pesira.backend.mapper;

import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    public <S, T> PageResponse<T> toPageResponse(Page<S> page, java.util.function.Function<S, T> mapper) {
        return PageResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public PageResponse<UserResponse> toUserPageResponse(Page<User> page, UserMapper userMapper) {
        return toPageResponse(page, userMapper::toResponse);
    }
}
