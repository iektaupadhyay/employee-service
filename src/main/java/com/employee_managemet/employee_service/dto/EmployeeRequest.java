package com.employee_managemet.employee_service.dto;

import com.employee_managemet.employee_service.enums.*;
import com.employee_managemet.employee_service.model.EmployeeAddress;

import java.time.LocalDate;

public record EmployeeRequest(
        String employeeCode,
        String name,
        Gender gender,
        EmploymentType employmentType,
        EmploymentStatus employmentStatus,
        LocalDate joiningDate,
        String mobileNumber,
        String email,
        String position,
        Long departmentId,
        EmployeeAddress address
) {}