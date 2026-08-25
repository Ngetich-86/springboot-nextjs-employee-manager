package com.pesira.backend.service.impl;

import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.employee.EmployeeRequest;
import com.pesira.backend.dto.employee.EmployeeResponse;
import com.pesira.backend.entity.Employee;
import com.pesira.backend.exception.BusinessException;
import com.pesira.backend.exception.ResourceNotFoundException;
import com.pesira.backend.mapper.EmployeeMapper;
import com.pesira.backend.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequest request;
    private Employee employee;

    @BeforeEach
    void setUp() {
        request = new EmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe@example.com");
        request.setDepartment("Engineering");
        request.setPosition("Engineer");
        request.setSalary(new BigDecimal("85000.00"));
        request.setHireDate(LocalDate.of(2024, 1, 15));

        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        employee.setEmail("jane.doe@example.com");
        employee.setDepartment("Engineering");
        employee.setPosition("Engineer");
        employee.setSalary(new BigDecimal("85000.00"));
        employee.setHireDate(LocalDate.of(2024, 1, 15));
    }

    @Test
    void createSavesAndReturnsMappedResponseWhenEmailIsUnique() {
        Employee unsavedEmployee = new Employee();
        EmployeeResponse expectedResponse = EmployeeResponse.builder()
                .id(1L)
                .email("jane.doe@example.com")
                .build();

        when(employeeRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(employeeMapper.toEntity(request)).thenReturn(unsavedEmployee);
        when(employeeRepository.save(unsavedEmployee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(expectedResponse);

        EmployeeResponse response = employeeService.create(request);

        assertThat(response).isEqualTo(expectedResponse);
        verify(employeeRepository).save(unsavedEmployee);
    }

    @Test
    void createThrowsConflictWhenEmailAlreadyExists() {
        when(employeeRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getMessage())
                            .isEqualTo("Employee with this email already exists");
                    assertThat(businessException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT.value());
                });
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void findByIdReturnsMappedResponseWhenFound() {
        EmployeeResponse expectedResponse = EmployeeResponse.builder().id(1L).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(expectedResponse);

        EmployeeResponse response = employeeService.findById(1L);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void findByIdThrowsResourceNotFoundExceptionWhenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found");
    }

    @Test
    void updateAppliesChangesAndReturnsMappedResponseWhenEmailIsAvailable() {
        EmployeeResponse expectedResponse = EmployeeResponse.builder().id(1L).firstName("Janet").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot("jane.doe@example.com", 1L)).thenReturn(false);
        when(employeeMapper.toResponse(employee)).thenReturn(expectedResponse);

        EmployeeResponse response = employeeService.update(1L, request);

        assertThat(response).isEqualTo(expectedResponse);
        verify(employeeMapper).updateEntity(employee, request);
    }

    @Test
    void updateThrowsResourceNotFoundExceptionWhenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found");
        verify(employeeMapper, never()).updateEntity(any(), any());
    }

    @Test
    void updateThrowsConflictWhenEmailBelongsToAnotherEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot("jane.doe@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getMessage())
                            .isEqualTo("Employee with this email already exists");
                    assertThat(businessException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT.value());
                });
        verify(employeeMapper, never()).updateEntity(any(), any());
    }

    @Test
    void deleteRemovesEmployeeWhenFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.delete(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteThrowsResourceNotFoundExceptionWhenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found");
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void findAllForExportMapsEachRepositoryResultInOrder() {
        Employee second = new Employee();
        second.setId(2L);
        EmployeeResponse firstResponse = EmployeeResponse.builder().id(1L).build();
        EmployeeResponse secondResponse = EmployeeResponse.builder().id(2L).build();

        when(employeeRepository.findAll(any(Specification.class))).thenReturn(List.of(employee, second));
        when(employeeMapper.toResponse(employee)).thenReturn(firstResponse);
        when(employeeMapper.toResponse(second)).thenReturn(secondResponse);

        List<EmployeeResponse> responses = employeeService.findAllForExport("engineering");

        assertThat(responses).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void findAllDelegatesToRepositoryAndMapsPagedResult() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);
        PageResponse<EmployeeResponse> expected = PageResponse.<EmployeeResponse>builder()
                .content(List.of(EmployeeResponse.builder().id(1L).build()))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(employeeMapper.toPageResponse(page)).thenReturn(expected);

        PageResponse<EmployeeResponse> response = employeeService.findAll("engineering", pageable);

        assertThat(response).isEqualTo(expected);
    }
}
