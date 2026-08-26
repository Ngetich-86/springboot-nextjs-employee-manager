package com.pesira.backend.service.impl;

import com.pesira.backend.config.CacheConfig;
import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.employee.EmployeeRequest;
import com.pesira.backend.dto.employee.EmployeeResponse;
import com.pesira.backend.entity.Employee;
import com.pesira.backend.exception.ResourceNotFoundException;
import com.pesira.backend.mapper.EmployeeMapper;
import com.pesira.backend.repository.EmployeeRepository;
import com.pesira.backend.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises EmployeeServiceImpl through Spring's real caching AOP proxy
 * (CacheConfig's actual CaffeineCacheManager), not a plain
 * "new EmployeeServiceImpl(...)" instantiation, since only a proxied bean
 * can prove @Cacheable/@CacheEvict actually intercept calls. Only
 * EmployeeRepository is mocked; EmployeeMapper is the real, stateless bean
 * so cache-hit vs fresh-fetch differences show up as real field values
 * rather than manually orchestrated stubs.
 */
@SpringBootTest(classes = {EmployeeServiceImpl.class, EmployeeMapper.class, CacheConfig.class})
@TestMethodOrder(MethodOrderer.Random.class)
class EmployeeServiceCacheTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames()
                .forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void findByIdCachesResultAndAvoidsRepeatedRepositoryCalls() {
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse first = employeeService.findById(1L);
        EmployeeResponse second = employeeService.findById(1L);

        assertThat(first.getFirstName()).isEqualTo("Jane");
        assertThat(second.getFirstName()).isEqualTo("Jane");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void updateEvictsEmployeeCacheSoSubsequentReadFetchesFreshData() {
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot(any(), eq(1L))).thenReturn(false);

        EmployeeResponse beforeUpdate = employeeService.findById(1L);
        assertThat(beforeUpdate.getFirstName()).isEqualTo("Jane");
        verify(employeeRepository, times(1)).findById(1L);

        EmployeeResponse updateResult = employeeService.update(1L, updateRequest());
        assertThat(updateResult.getFirstName()).isEqualTo("Janet");
        verify(employeeRepository, times(2)).findById(1L);

        EmployeeResponse afterUpdate = employeeService.findById(1L);
        assertThat(afterUpdate.getFirstName())
                .as("cache must not serve the pre-update value after an eviction")
                .isEqualTo("Janet");
        verify(employeeRepository, times(3)).findById(1L);
    }

    @Test
    void deleteEvictsEmployeeCacheSoSubsequentReadReflectsDeletion() {
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse beforeDelete = employeeService.findById(1L);
        assertThat(beforeDelete.getFirstName()).isEqualTo("Jane");
        verify(employeeRepository, times(1)).findById(1L);

        employeeService.delete(1L);
        verify(employeeRepository, times(2)).findById(1L);
        verify(employeeRepository).delete(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(1L))
                .as("a deleted employee must not be served from a stale cache entry")
                .isInstanceOf(ResourceNotFoundException.class);
        verify(employeeRepository, times(3)).findById(1L);
    }

    @Test
    void findAllCachesPagedResultsAndAvoidsRepeatedRepositoryCalls() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee employee = sampleEmployee();
        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 1));

        PageResponse<EmployeeResponse> first = employeeService.findAll(null, pageable);
        PageResponse<EmployeeResponse> second = employeeService.findAll(null, pageable);

        assertThat(first.getTotalElements()).isEqualTo(1);
        assertThat(second.getTotalElements()).isEqualTo(1);
        verify(employeeRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void differentPageableProducesDistinctCacheEntry() {
        Pageable pageOne = PageRequest.of(0, 10);
        Pageable pageTwo = PageRequest.of(1, 10);
        Employee employee = sampleEmployee();
        when(employeeRepository.findAll(any(Specification.class), eq(pageOne)))
                .thenReturn(new PageImpl<>(List.of(employee), pageOne, 1));
        when(employeeRepository.findAll(any(Specification.class), eq(pageTwo)))
                .thenReturn(new PageImpl<>(List.of(), pageTwo, 0));

        employeeService.findAll(null, pageOne);
        employeeService.findAll(null, pageTwo);

        verify(employeeRepository, times(1)).findAll(any(Specification.class), eq(pageOne));
        verify(employeeRepository, times(1)).findAll(any(Specification.class), eq(pageTwo));
    }

    @Test
    void createEvictsBothEmployeeAndPageCaches() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 1));

        employeeService.findById(1L);
        employeeService.findAll(null, pageable);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).findAll(any(Specification.class), eq(pageable));

        Employee savedNewEmployee = new Employee();
        savedNewEmployee.setId(2L);
        when(employeeRepository.existsByEmail("new.hire@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedNewEmployee);

        employeeService.create(newHireRequest());

        Employee secondEmployee = sampleEmployee();
        secondEmployee.setId(2L);
        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(employee, secondEmployee), pageable, 2));

        employeeService.findById(1L);
        PageResponse<EmployeeResponse> pageAfterCreate = employeeService.findAll(null, pageable);

        verify(employeeRepository, times(2)).findById(1L);
        verify(employeeRepository, times(2)).findAll(any(Specification.class), eq(pageable));
        assertThat(pageAfterCreate.getTotalElements())
                .as("the newly created employee must be reflected once the page cache is evicted")
                .isEqualTo(2);
    }

    private Employee sampleEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        employee.setEmail("jane.doe@example.com");
        employee.setDepartment("Engineering");
        employee.setPosition("Engineer");
        employee.setSalary(new BigDecimal("85000.00"));
        employee.setHireDate(LocalDate.of(2024, 1, 15));
        return employee;
    }

    private EmployeeRequest updateRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("Janet");
        request.setLastName("Doe");
        request.setEmail("jane.doe@example.com");
        request.setDepartment("Engineering");
        request.setPosition("Senior Engineer");
        request.setSalary(new BigDecimal("95000.00"));
        request.setHireDate(LocalDate.of(2024, 3, 1));
        return request;
    }

    private EmployeeRequest newHireRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("New");
        request.setLastName("Hire");
        request.setEmail("new.hire@example.com");
        request.setDepartment("Engineering");
        request.setPosition("Engineer");
        request.setSalary(new BigDecimal("80000.00"));
        request.setHireDate(LocalDate.of(2024, 6, 1));
        return request;
    }
}
