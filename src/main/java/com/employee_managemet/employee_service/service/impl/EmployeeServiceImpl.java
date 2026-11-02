package com.employee_managemet.employee_service.service.impl;

import com.employee_managemet.employee_service.constants.LogTags;
import com.employee_managemet.employee_service.dto.EmployeeRequest;
import com.employee_managemet.employee_service.dto.EmployeeResponse;
import com.employee_managemet.employee_service.enums.EmploymentStatus;
import com.employee_managemet.employee_service.model.Department;
import com.employee_managemet.employee_service.model.Employee;
import com.employee_managemet.employee_service.model.EmployeeAddress;
import com.employee_managemet.employee_service.repository.DepartmentRepository;
import com.employee_managemet.employee_service.repository.EmployeeRepository;
import com.employee_managemet.employee_service.service.EmployeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        log.info("[{}] Creating employee. employeeCode={}",
                LogTags.SERVICE, request.employeeCode());

        validateDuplicateEmployeeCode(request.employeeCode());
        validateDuplicateEmail(request.email());

        Department department = findActiveDepartment(request.departmentId());

        Employee employee = new Employee();

        mapRequestToEmployee(request, employee, department);

        Employee savedEmployee = employeeRepository.save(employee);

        log.info("[{}] Employee created successfully. employeeId={}",
                LogTags.SERVICE, savedEmployee.getId());

        return mapToResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {

        log.debug("[{}] Fetching employee. employeeId={}",
                LogTags.SERVICE, id);

        Employee employee = findEmployeeById(id);

        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String employeeCode) {

        log.debug("[{}] Fetching employee by employeeCode={}",
                LogTags.SERVICE, employeeCode);

        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> {

                    log.warn("[{}] Employee not found. employeeCode={}",
                            LogTags.DB_OP, employeeCode);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Employee not found with code: " + employeeCode
                    );
                });

        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {

        log.debug("[{}] Fetching employees. page={}, size={}",
                LogTags.SERVICE,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<EmployeeResponse> employees =
                employeeRepository.findAll(pageable)
                        .map(this::mapToResponse);

        log.info("[{}] Employees fetched successfully. totalElements={}",
                LogTags.SERVICE, employees.getTotalElements());

        return employees;
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {

        log.info("[{}] Updating employee. employeeId={}",
                LogTags.SERVICE, id);

        Employee employee = findEmployeeById(id);

        validateDuplicateEmployeeCodeForUpdate(
                request.employeeCode(), id);

        validateDuplicateEmailForUpdate(
                request.email(), id);

        Department department = findActiveDepartment(request.departmentId());

        mapRequestToEmployee(request, employee, department);

        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("[{}] Employee updated successfully. employeeId={}",
                LogTags.SERVICE, updatedEmployee.getId());

        return mapToResponse(updatedEmployee);
    }

    @Override
    public void deactivateEmployee(Long id) {

        log.info("[{}] Deactivating employee. employeeId={}",
                LogTags.SERVICE, id);

        Employee employee = findEmployeeById(id);

        if (employee.getEmploymentStatus() == EmploymentStatus.INACTIVE) {

            log.warn("[{}] Employee is already inactive. employeeId={}",
                    LogTags.SERVICE, id);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee is already inactive"
            );
        }

        employee.setEmploymentStatus(EmploymentStatus.INACTIVE);

        employeeRepository.save(employee);

        log.info("[{}] Employee deactivated successfully. employeeId={}",
                LogTags.SERVICE, id);
    }

    // -------------------- Helper Methods --------------------

    private Employee findEmployeeById(Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn("[{}] Employee not found. employeeId={}",
                            LogTags.DB_OP, id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Employee not found with id: " + id
                    );
                });
    }

    private Department findActiveDepartment(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> {

                    log.warn("[{}] Department not found. departmentId={}",
                            LogTags.DB_OP, departmentId);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Department not found with id: " + departmentId
                    );
                });

        if (!Boolean.TRUE.equals(department.getActive())) {

            log.warn("[{}] Inactive department cannot be assigned. departmentId={}",
                    LogTags.SERVICE, departmentId);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign an inactive department to an employee"
            );
        }

        return department;
    }

    private void validateDuplicateEmployeeCode(String employeeCode) {

        if (employeeRepository.existsByEmployeeCode(employeeCode)) {

            log.warn("[{}] Duplicate employee code detected",
                    LogTags.SERVICE);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee code already exists"
            );
        }
    }

    private void validateDuplicateEmail(String email) {

        if (employeeRepository.existsByEmail(email)) {

            log.warn("[{}] Duplicate employee email detected",
                    LogTags.SERVICE);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee email already exists"
            );
        }
    }

    private void validateDuplicateEmployeeCodeForUpdate(
            String employeeCode, Long id) {

        employeeRepository.findByEmployeeCode(employeeCode)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {

                    log.warn("[{}] Employee code already belongs to another employee",
                            LogTags.SERVICE);

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Employee code already exists"
                    );
                });
    }

    private void validateDuplicateEmailForUpdate(String email, Long id) {

        employeeRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {

                    log.warn("[{}] Employee email already belongs to another employee",
                            LogTags.SERVICE);

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Employee email already exists"
                    );
                });
    }

    private void mapRequestToEmployee(
            EmployeeRequest request,
            Employee employee,
            Department department) {

        employee.setEmployeeCode(request.employeeCode());
        employee.setName(request.name());
        employee.setGender(request.gender());
        employee.setEmploymentType(request.employmentType());
        employee.setEmploymentStatus(request.employmentStatus());
        employee.setJoiningDate(request.joiningDate());
        employee.setMobileNumber(request.mobileNumber());
        employee.setEmail(request.email());
        employee.setPosition(request.position());
        employee.setDepartment(department);

        EmployeeAddress address = request.address();
        employee.setAddress(address);
    }

    private EmployeeResponse mapToResponse(Employee employee) {

        EmployeeResponse.AddressResponse addressResponse = null;

        if (employee.getAddress() != null) {

            EmployeeAddress address = employee.getAddress();

            addressResponse = new EmployeeResponse.AddressResponse(
                    address.getAddressLine1(),
                    address.getAddressLine2(),
                    address.getCity(),
                    address.getState(),
                    address.getCountry(),
                    address.getPostalCode()
            );
        }

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getGender(),
                employee.getEmploymentType(),
                employee.getEmploymentStatus(),
                employee.getJoiningDate(),
                employee.getMobileNumber(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getDepartment().getId(),
                employee.getDepartment().getName(),
                addressResponse
        );
    }
}