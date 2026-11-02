
package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.constants.LogTags;
import com.employee_managemet.employee_service.dto.EmployeeRequest;
import com.employee_managemet.employee_service.dto.EmployeeResponse;
import com.employee_managemet.employee_service.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/createemployee")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        log.info("[{}] POST /api/v1/employees/createemployee",
                LogTags.CONTROLLER);

        EmployeeResponse response =
                employeeService.createEmployee(request);

        log.info("[{}] Employee creation completed. employeeId={}",
                LogTags.CONTROLLER, response.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getemployeebyid/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable Long id) {

        log.info("[{}] GET employee by ID. employeeId={}",
                LogTags.CONTROLLER, id);

        return ResponseEntity.ok(
                employeeService.getEmployeeById(id)
        );
    }

    @GetMapping("/getemployeebycode/{employeeCode}")
    public ResponseEntity<EmployeeResponse> getEmployeeByCode(
            @PathVariable String employeeCode) {

        log.info("[{}] GET employee by code",
                LogTags.CONTROLLER);

        return ResponseEntity.ok(
                employeeService.getEmployeeByCode(employeeCode)
        );
    }

    @GetMapping("/getallemployees")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        log.info("[{}] GET all employees. page={}, size={}",
                LogTags.CONTROLLER,
                pageable.getPageNumber(),
                pageable.getPageSize());

        return ResponseEntity.ok(
                employeeService.getAllEmployees(pageable)
        );
    }

    @PutMapping("/updateemployee/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {

        log.info("[{}] PUT update employee. employeeId={}",
                LogTags.CONTROLLER, id);

        return ResponseEntity.ok(
                employeeService.updateEmployee(id, request)
        );
    }

    @DeleteMapping("/deactivateemployee/{id}")
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable Long id) {

        log.info("[{}] DELETE deactivate employee. employeeId={}",
                LogTags.CONTROLLER, id);

        employeeService.deactivateEmployee(id);

        return ResponseEntity.noContent().build();
    }
}
