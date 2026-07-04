package com.pesira.backend.controller;

import com.pesira.backend.dto.ApiResponse;
import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.employee.EmployeeRequest;
import com.pesira.backend.dto.employee.EmployeeResponse;
import com.pesira.backend.service.EmployeeService;
import com.pesira.backend.util.ApiConstants;
import com.pesira.backend.util.EmployeeExportUtils;
import com.pesira.backend.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.EMPLOYEES_API)
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeExportUtils employeeExportUtils;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List employees with pagination, sorting, and search")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        PageResponse<EmployeeResponse> employees = employeeService.findAll(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> findById(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse employee = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse employee = employeeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", employee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Export employees as CSV")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) String search) {
        List<EmployeeResponse> employees = employeeService.findAllForExport(search);
        byte[] csvContent = employeeExportUtils.toCsv(employees);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv")
                .contentType(new MediaType("text", "csv"))
                .body(csvContent);
    }

    @GetMapping("/export/json")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Export employees as JSON")
    public ResponseEntity<byte[]> exportJson(@RequestParam(required = false) String search) throws IOException {
        List<EmployeeResponse> employees = employeeService.findAllForExport(search);
        byte[] jsonContent = employeeExportUtils.toJson(employees);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonContent);
    }
}
