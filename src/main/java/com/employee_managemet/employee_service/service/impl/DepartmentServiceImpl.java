package com.employee_managemet.employee_service.service.impl;

import com.employee_managemet.employee_service.constants.LogTags;
import com.employee_managemet.employee_service.dto.DepartmentRequest;
import com.employee_managemet.employee_service.dto.DepartmentResponse;
import com.employee_managemet.employee_service.model.Department;
import com.employee_managemet.employee_service.repository.DepartmentRepository;
import com.employee_managemet.employee_service.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {

        log.info("[{}] Creating department. name={}",
                LogTags.SERVICE, request.name());

        departmentRepository.findByNameIgnoreCase(request.name())
                .ifPresent(department -> {
                    log.warn("[{}] Department name already exists",
                            LogTags.SERVICE);

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Department name already exists"
                    );
                });

        Department department = new Department();
        department.setName(request.name());
        department.setDescription(request.description());
        department.setActive(true);

        Department savedDepartment = departmentRepository.save(department);

        log.info("[{}] Department created successfully. departmentId={}",
                LogTags.SERVICE, savedDepartment.getId());

        return mapToResponse(savedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {

        log.debug("[{}] Fetching department. departmentId={}",
                LogTags.SERVICE, id);

        return mapToResponse(findDepartmentById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {

        log.debug("[{}] Fetching all departments", LogTags.SERVICE);

        List<DepartmentResponse> departments = departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("[{}] Departments fetched. count={}",
                LogTags.SERVICE, departments.size());

        return departments;
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {

        log.info("[{}] Updating department. departmentId={}",
                LogTags.SERVICE, id);

        Department department = findDepartmentById(id);

        if (!Boolean.TRUE.equals(department.getActive())) {
            log.warn("[{}] Attempt to update inactive department. departmentId={}",
                    LogTags.SERVICE, id);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inactive department cannot be updated"
            );
        }

        departmentRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    log.warn("[{}] Department name already used by another department",
                            LogTags.SERVICE);

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Department name already exists"
                    );
                });

        department.setName(request.name());
        department.setDescription(request.description());

        Department updatedDepartment = departmentRepository.save(department);

        log.info("[{}] Department updated successfully. departmentId={}",
                LogTags.SERVICE, id);

        return mapToResponse(updatedDepartment);
    }

    @Override
    public void deactivateDepartment(Long id) {

        log.info("[{}] Deactivating department. departmentId={}",
                LogTags.SERVICE, id);

        Department department = findDepartmentById(id);

        if (!Boolean.TRUE.equals(department.getActive())) {
            log.warn("[{}] Department is already inactive. departmentId={}",
                    LogTags.SERVICE, id);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department is already inactive"
            );
        }

        department.setActive(false);
        departmentRepository.save(department);

        log.info("[{}] Department deactivated successfully. departmentId={}",
                LogTags.SERVICE, id);
    }

    private Department findDepartmentById(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}] Department not found. departmentId={}",
                            LogTags.DB_OP, id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Department not found with id: " + id
                    );
                });
    }

    private DepartmentResponse mapToResponse(Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getActive()
        );
    }
}