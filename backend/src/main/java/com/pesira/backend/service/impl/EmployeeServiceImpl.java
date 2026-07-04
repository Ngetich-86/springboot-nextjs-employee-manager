package com.pesira.backend.service.impl;

import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.employee.EmployeeRequest;
import com.pesira.backend.dto.employee.EmployeeResponse;
import com.pesira.backend.entity.Employee;
import com.pesira.backend.exception.BusinessException;
import com.pesira.backend.exception.ResourceNotFoundException;
import com.pesira.backend.mapper.EmployeeMapper;
import com.pesira.backend.repository.EmployeeRepository;
import com.pesira.backend.repository.EmployeeSpecifications;
import com.pesira.backend.service.EmployeeService;
import com.pesira.backend.util.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Cacheable(
            value = CacheNames.EMPLOYEE_PAGES,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort + '-' + (#search == null ? '' : #search)")
    public PageResponse<EmployeeResponse> findAll(String search, Pageable pageable) {
        Specification<Employee> specification = EmployeeSpecifications.withSearch(search);
        Page<Employee> page = employeeRepository.findAll(specification, pageable);
        return employeeMapper.toPageResponse(page);
    }

    @Override
    @Cacheable(value = CacheNames.EMPLOYEES, key = "#id")
    public EmployeeResponse findById(Long id) {
        Employee employee = getEmployeeOrThrow(id);
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheNames.EMPLOYEES, CacheNames.EMPLOYEE_PAGES}, allEntries = true)
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Employee with this email already exists", HttpStatus.CONFLICT.value());
        }

        Employee employee = employeeMapper.toEntity(request);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheNames.EMPLOYEES, CacheNames.EMPLOYEE_PAGES}, allEntries = true)
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getEmployeeOrThrow(id);

        if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException("Employee with this email already exists", HttpStatus.CONFLICT.value());
        }

        employeeMapper.updateEntity(employee, request);
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheNames.EMPLOYEES, CacheNames.EMPLOYEE_PAGES}, allEntries = true)
    public void delete(Long id) {
        Employee employee = getEmployeeOrThrow(id);
        employeeRepository.delete(employee);
    }

    @Override
    public List<EmployeeResponse> findAllForExport(String search) {
        Specification<Employee> specification = EmployeeSpecifications.withSearch(search);
        return employeeRepository.findAll(specification)
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    private Employee getEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
}
