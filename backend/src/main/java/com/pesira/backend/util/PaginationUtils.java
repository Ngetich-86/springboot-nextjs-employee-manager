package com.pesira.backend.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PaginationUtils() {
    }

    public static Pageable createPageable(Integer page, Integer size, String sortBy, String sortDirection) {
        int resolvedPage = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int resolvedSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        Sort sort = buildSort(sortBy, sortDirection);
        return PageRequest.of(resolvedPage, resolvedSize, sort);
    }

    private static Sort buildSort(String sortBy, String sortDirection) {
        String property = sortBy != null && !sortBy.isBlank() ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
