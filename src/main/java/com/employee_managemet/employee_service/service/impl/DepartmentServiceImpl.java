package com.employee_managemet.employee_service.service.impl;

import com.employee_managemet.employee_service.dto.DepartmentRequest;
import com.employee_managemet.employee_service.dto.DepartmentResponse;
import com.employee_managemet.employee_service.model.Department;
import com.employee_managemet.employee_service.repository.DepartmentRepository;
import com.employee_managemet.employee_service.service.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public DepartmentResponse createDepartment(
            DepartmentRequest request) {

        validateDepartment(request);

        if (departmentRepository.existsByNameIgnoreCase(
                request.name().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department already exists");
        }

        Department department = new Department();
        department.setName(request.name().trim());
        department.setDescription(request.description());
        department.setActive(true);

        return mapToResponse(
                departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {

        return mapToResponse(findDepartment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DepartmentResponse updateDepartment(
            Long id, DepartmentRequest request) {

        validateDepartment(request);

        Department department = findDepartment(id);

        departmentRepository.findByNameIgnoreCase(
                        request.name().trim())
                .filter(existing ->
                        !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Department name already exists");
                });

        if (!Boolean.TRUE.equals(department.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot update an inactive department");
        }

        department.setName(request.name().trim());
        department.setDescription(request.description());

        return mapToResponse(
                departmentRepository.save(department));
    }

    @Override
    public void deactivateDepartment(Long id) {

        Department department = findDepartment(id);

        department.setActive(false);

        departmentRepository.save(department);
    }

    private Department findDepartment(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found"));
    }

    private void validateDepartment(DepartmentRequest request) {

        if (request == null || request.name() == null
                || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Department name is required");
        }
    }

    private DepartmentResponse mapToResponse(
            Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getActive()
        );
    }
}