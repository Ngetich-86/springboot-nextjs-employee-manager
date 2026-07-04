package com.pesira.backend.dto.employee;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class EmployeeResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String department;
    private final String position;
    private final BigDecimal salary;
    private final LocalDate hireDate;
    private final Instant createdAt;
    private final Instant updatedAt;
}
