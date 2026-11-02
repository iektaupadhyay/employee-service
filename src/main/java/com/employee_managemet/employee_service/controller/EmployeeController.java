package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.dto.EmployeeRequest;
import com.employee_managemet.employee_service.dto.EmployeeResponse;
import com.employee_managemet.employee_service.service.EmployeeService;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @RequestBody EmployeeRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                employeeService.getEmployeeById(id));
    }

    @GetMapping("/code/{employeeCode}")
    public ResponseEntity<EmployeeResponse> getEmployeeByCode(
            @PathVariable String employeeCode) {

        return ResponseEntity.ok(
                employeeService.getEmployeeByCode(employeeCode));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {

        return ResponseEntity.ok(
                employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable Long id) {

        employeeService.deactivateEmployee(id);

        return ResponseEntity.noContent().build();
    }
}