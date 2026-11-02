package com.employee_managemet.employee_service.service.impl;

import com.employee_managemet.employee_service.dto.EmployeeRequest;
import com.employee_managemet.employee_service.dto.EmployeeResponse;
import com.employee_managemet.employee_service.model.*;
import com.employee_managemet.employee_service.repository.*;
import com.employee_managemet.employee_service.service.EmployeeService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository) {

        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        if (employeeRepository.existsByEmployeeCode(
                request.employeeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee code already exists");
        }

        if (employeeRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists");
        }

        Department department = departmentRepository
                .findById(request.departmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found"));

        if (!Boolean.TRUE.equals(department.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign an inactive department");
        }

        Employee employee = new Employee();

        mapRequestToEmployee(request, employee);
        employee.setDepartment(department);

        Employee savedEmployee = employeeRepository.save(employee);

        return mapToResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String employeeCode) {

        Employee employee = employeeRepository
                .findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {

        return employeeRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public EmployeeResponse updateEmployee(
            Long id, EmployeeRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        employeeRepository.findByEmployeeCode(request.employeeCode())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Employee code already exists");
                });

        employeeRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email already exists");
                });

        mapRequestToEmployee(request, employee);

        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    public void deactivateEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setEmploymentStatus(
                com.employee_managemet.employee_service.enums
                        .EmploymentStatus.INACTIVE);

        employeeRepository.save(employee);
    }

    private void mapRequestToEmployee(
            EmployeeRequest request, Employee employee) {

        employee.setEmployeeCode(request.employeeCode());
        employee.setName(request.name());
        employee.setGender(request.gender());
        employee.setEmploymentType(request.employmentType());
        employee.setEmploymentStatus(request.employmentStatus());
        employee.setJoiningDate(request.joiningDate());
        employee.setMobileNumber(request.mobileNumber());
        employee.setEmail(request.email());
        employee.setPosition(request.position());
        employee.setAddress(request.address());

        Department department = departmentRepository
                .findById(request.departmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found"));

        employee.setDepartment(department);
    }

    private EmployeeResponse mapToResponse(Employee employee) {

        EmployeeAddress address = employee.getAddress();

        EmployeeResponse.AddressResponse addressResponse = null;

        if (address != null) {
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
                employee.getDepartment() != null
                        ? employee.getDepartment().getId() : null,
                employee.getDepartment() != null
                        ? employee.getDepartment().getName() : null,
                addressResponse
        );
    }
}