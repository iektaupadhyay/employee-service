package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.dto.EmployeeLeaveRequest;
import com.employee_managemet.employee_service.dto.EmployeeLeaveResponse;
import com.employee_managemet.employee_service.service.EmployeeLeaveService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-leaves")
public class EmployeeLeaveController {

    private final EmployeeLeaveService leaveService;

    public EmployeeLeaveController(
            EmployeeLeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<EmployeeLeaveResponse> createAllocation(
            @RequestBody EmployeeLeaveRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.createLeaveAllocation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeLeaveResponse> getAllocation(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                leaveService.getLeaveAllocation(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeLeaveResponse>> getEmployeeLeaves(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                leaveService.getEmployeeLeaves(employeeId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeLeaveResponse> updateAllocation(
            @PathVariable Long id,
            @RequestBody EmployeeLeaveRequest request) {

        return ResponseEntity.ok(
                leaveService.updateLeaveAllocation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllocation(
            @PathVariable Long id) {

        leaveService.deleteLeaveAllocation(id);

        return ResponseEntity.noContent().build();
    }
}