package com.employee_managemet.employee_service.service;

import com.employee_managemet.employee_service.dto.EmployeeRequest;
import com.employee_managemet.employee_service.dto.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByCode(String employeeCode);

    Page<EmployeeResponse> getAllEmployees(Pageable pageable);

    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    void deactivateEmployee(Long id);
}