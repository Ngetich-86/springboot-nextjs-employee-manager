package com.pesira.backend.service;

import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.employee.EmployeeRequest;
import com.pesira.backend.dto.employee.EmployeeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    PageResponse<EmployeeResponse> findAll(String search, Pageable pageable);

    EmployeeResponse findById(Long id);

    EmployeeResponse create(EmployeeRequest request);

    EmployeeResponse update(Long id, EmployeeRequest request);

    void delete(Long id);

    List<EmployeeResponse> findAllForExport(String search);
}
