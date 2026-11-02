package com.employee_managemet.employee_service.dto;

import com.employee_managemet.employee_service.enums.*;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
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
        String departmentName,
        AddressResponse address
) {
    public record AddressResponse(
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String country,
            String postalCode
    ) {}
}