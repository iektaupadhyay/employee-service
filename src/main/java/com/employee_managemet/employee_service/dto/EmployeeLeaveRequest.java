package com.employee_managemet.employee_service.dto;

import com.employee_managemet.employee_service.enums.LeaveType;

import java.time.LocalDate;

public record EmployeeLeaveRequest(
        Long employeeId,
        LeaveType leaveType,
        Integer totalLeaves,
        LocalDate appliedDate
) {}