package com.employee_managemet.employee_service.model;


import com.employee_managemet.employee_service.enums.EmploymentStatus;
import com.employee_managemet.employee_service.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import com.employee_managemet.employee_service.enums.Gender;
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Embedded
    private EmployeeAddress address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 100)
    private String position;

    @Version
    private Long version;
}
