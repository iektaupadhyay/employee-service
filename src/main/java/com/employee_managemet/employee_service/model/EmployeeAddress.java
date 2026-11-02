package com.employee_managemet.employee_service.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class EmployeeAddress {

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 150)
    private String addressLine1;

    @Size(max = 150)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 80)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 80)
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 80)
    private String country;

    @NotBlank(message = "Postal code is required")
    @Size(max = 15)
    private String postalCode;
}