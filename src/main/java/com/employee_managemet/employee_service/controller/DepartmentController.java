package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.dto.DepartmentRequest;
import com.employee_managemet.employee_service.dto.DepartmentResponse;
import com.employee_managemet.employee_service.service.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(
            DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @RequestBody DepartmentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                departmentService.getDepartmentById(id));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {

        return ResponseEntity.ok(
                departmentService.getAllDepartments());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentRequest request) {

        return ResponseEntity.ok(
                departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDepartment(
            @PathVariable Long id) {

        departmentService.deactivateDepartment(id);

        return ResponseEntity.noContent().build();
    }
}