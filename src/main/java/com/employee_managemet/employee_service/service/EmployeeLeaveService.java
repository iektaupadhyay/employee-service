package com.employee_managemet.employee_service.service;

import com.employee_managemet.employee_service.dto.EmployeeLeaveRequest;
import com.employee_managemet.employee_service.dto.EmployeeLeaveResponse;

import java.util.List;

public interface EmployeeLeaveService {

    EmployeeLeaveResponse createLeaveAllocation(
            EmployeeLeaveRequest request);

    EmployeeLeaveResponse getLeaveAllocation(Long id);

    List<EmployeeLeaveResponse> getEmployeeLeaves(Long employeeId);

    EmployeeLeaveResponse updateLeaveAllocation(
            Long id, EmployeeLeaveRequest request);

    void deleteLeaveAllocation(Long id);
}