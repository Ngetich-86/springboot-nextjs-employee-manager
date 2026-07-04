package com.pesira.backend.repository;

import com.pesira.backend.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> withSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("position")), pattern));
        };
    }
}
