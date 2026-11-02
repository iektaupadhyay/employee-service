package com.employee_managemet.employee_service.dto;

import com.employee_managemet.employee_service.enums.EmploymentStatus;
import com.employee_managemet.employee_service.enums.EmploymentType;
import com.employee_managemet.employee_service.enums.Gender;
import com.employee_managemet.employee_service.model.EmployeeAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeRequest(

        @NotBlank(message = "Employee code is required")
        @Size(max = 30, message = "Employee code cannot exceed 30 characters")
        String employeeCode,

        @NotBlank(message = "Employee name is required")
        @Size(max = 100, message = "Employee name cannot exceed 100 characters")
        String name,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotNull(message = "Employment type is required")
        EmploymentType employmentType,

        @NotNull(message = "Employment status is required")
        EmploymentStatus employmentStatus,

        @NotNull(message = "Joining date is required")
        LocalDate joiningDate,

        @NotBlank(message = "Mobile number is required")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Mobile number must contain 10 to 15 digits, optionally prefixed with +"
        )
        String mobileNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @NotBlank(message = "Position is required")
        @Size(max = 100, message = "Position cannot exceed 100 characters")
        String position,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotNull(message = "Address is required")
        @Valid
        EmployeeAddress address

) {
}