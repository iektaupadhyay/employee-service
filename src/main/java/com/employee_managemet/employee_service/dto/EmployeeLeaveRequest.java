package com.employee_managemet.employee_service.dto;

import com.employee_managemet.employee_service.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record EmployeeLeaveRequest(

        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Leave type is required")
        LeaveType leaveType,

        @NotNull(message = "Total leaves is required")
        @PositiveOrZero(message = "Total leaves cannot be negative")
        Integer totalLeaves,

        @NotNull(message = "Applied date is required")
        LocalDate appliedDate

) {
}