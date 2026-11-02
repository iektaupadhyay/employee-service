package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.constants.LogTags;
import com.employee_managemet.employee_service.dto.DepartmentRequest;
import com.employee_managemet.employee_service.dto.DepartmentResponse;
import com.employee_managemet.employee_service.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        log.info("[{}] POST /api/v1/departments", LogTags.CONTROLLER);

        DepartmentResponse response =
                departmentService.createDepartment(request);

        log.info("[{}] Department creation completed. departmentId={}",
                LogTags.CONTROLLER, response.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id) {

        log.info("[{}] GET /api/v1/departments/{}", LogTags.CONTROLLER, id);

        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {

        log.info("[{}] GET /api/v1/departments", LogTags.CONTROLLER);

        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        log.info("[{}] PUT /api/v1/departments/{}", LogTags.CONTROLLER, id);

        return ResponseEntity.ok(
                departmentService.updateDepartment(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDepartment(@PathVariable Long id) {

        log.info("[{}] DELETE /api/v1/departments/{}", LogTags.CONTROLLER, id);

        departmentService.deactivateDepartment(id);

        return ResponseEntity.noContent().build();
    }
}