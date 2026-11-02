package com.employee_managemet.employee_service.dto;

import com.employee_managemet.employee_service.enums.LeaveType;

import java.time.LocalDate;

public record EmployeeLeaveResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LeaveType leaveType,
        Integer totalLeaves,
        LocalDate appliedDate
) {}