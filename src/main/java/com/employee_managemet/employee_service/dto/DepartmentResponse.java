package com.employee_managemet.employee_service.dto;

public record DepartmentResponse(
        Long id,
        String name,
        String description,
        Boolean active
) {}