package com.pesira.backend.mapper;

import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.employee.EmployeeRequest;
import com.pesira.backend.dto.employee.EmployeeResponse;
import com.pesira.backend.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        updateEntity(employee, request);
        return employee;
    }

    public void updateEntity(Employee employee, EmployeeRequest request) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
    }

    public EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public PageResponse<EmployeeResponse> toPageResponse(Page<Employee> page) {
        return PageResponse.<EmployeeResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
