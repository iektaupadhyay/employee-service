
package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.constants.LogTags;
import com.employee_managemet.employee_service.dto.EmployeeLeaveRequest;
import com.employee_managemet.employee_service.dto.EmployeeLeaveResponse;
import com.employee_managemet.employee_service.service.EmployeeLeaveService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee-leaves")
public class EmployeeLeaveController {

    private final EmployeeLeaveService leaveService;

    public EmployeeLeaveController(
            EmployeeLeaveService leaveService) {

        this.leaveService = leaveService;
    }

    @PostMapping("/createleaveallocation")
    public ResponseEntity<EmployeeLeaveResponse> createAllocation(
            @RequestBody EmployeeLeaveRequest request) {

        log.info("[{}] POST /api/v1/employee-leaves/createleaveallocation",
                LogTags.CONTROLLER);

        EmployeeLeaveResponse response =
                leaveService.createLeaveAllocation(request);

        log.info("[{}] Leave allocation created. allocationId={}",
                LogTags.CONTROLLER, response.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/getleaveallocation/{id}")
    public ResponseEntity<EmployeeLeaveResponse> getAllocation(
            @PathVariable Long id) {

        log.info("[{}] GET leave allocation. allocationId={}",
                LogTags.CONTROLLER, id);

        return ResponseEntity.ok(
                leaveService.getLeaveAllocation(id)
        );
    }

    @GetMapping("/getemployeeleaves/{employeeId}")
    public ResponseEntity<List<EmployeeLeaveResponse>> getEmployeeLeaves(
            @PathVariable Long employeeId) {

        log.info("[{}] GET employee leaves. employeeId={}",
                LogTags.CONTROLLER, employeeId);

        return ResponseEntity.ok(
                leaveService.getEmployeeLeaves(employeeId)
        );
    }

    @PutMapping("/updateleaveallocation/{id}")
    public ResponseEntity<EmployeeLeaveResponse> updateAllocation(
            @PathVariable Long id,
            @RequestBody EmployeeLeaveRequest request) {

        log.info("[{}] PUT update leave allocation. allocationId={}",
                LogTags.CONTROLLER, id);

        return ResponseEntity.ok(
                leaveService.updateLeaveAllocation(id, request)
        );
    }

    @DeleteMapping("/deleteleaveallocation/{id}")
    public ResponseEntity<Void> deleteAllocation(
            @PathVariable Long id) {

        log.info("[{}] DELETE leave allocation. allocationId={}",
                LogTags.CONTROLLER, id);

        leaveService.deleteLeaveAllocation(id);

        return ResponseEntity.noContent().build();
    }
}
