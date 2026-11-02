package com.employee_managemet.employee_service.service;

import com.employee_managemet.employee_service.dto.DepartmentRequest;
import com.employee_managemet.employee_service.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse getDepartmentById(Long id);

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse updateDepartment(
            Long id, DepartmentRequest request);

    void deactivateDepartment(Long id);
}