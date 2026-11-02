package com.employee_managemet.employee_service.service.impl;

import com.employee_managemet.employee_service.constants.LogTags;
import com.employee_managemet.employee_service.dto.EmployeeLeaveRequest;
import com.employee_managemet.employee_service.dto.EmployeeLeaveResponse;
import com.employee_managemet.employee_service.model.Employee;
import com.employee_managemet.employee_service.model.EmployeeLeave;
import com.employee_managemet.employee_service.repository.EmployeeLeaveRepository;
import com.employee_managemet.employee_service.repository.EmployeeRepository;
import com.employee_managemet.employee_service.service.EmployeeLeaveService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@Transactional
public class EmployeeLeaveServiceImpl
        implements EmployeeLeaveService {

    private final EmployeeLeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeLeaveServiceImpl(
            EmployeeLeaveRepository leaveRepository,
            EmployeeRepository employeeRepository) {

        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeLeaveResponse createLeaveAllocation(
            EmployeeLeaveRequest request) {

        log.info("[{}] Creating leave allocation",
                LogTags.SERVICE);

        validateRequest(request);

        Employee employee = findEmployee(request.employeeId());

        if (leaveRepository.existsByEmployeeIdAndLeaveType(
                request.employeeId(), request.leaveType())) {

            log.warn("[{}] Leave allocation already exists. employeeId={}, leaveType={}",
                    LogTags.SERVICE,
                    request.employeeId(),
                    request.leaveType());

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leave allocation already exists for this employee and leave type");
        }

        EmployeeLeave leave = new EmployeeLeave();

        leave.setEmployee(employee);
        leave.setLeaveType(request.leaveType());
        leave.setTotalLeaves(request.totalLeaves());
        leave.setAppliedDate(request.appliedDate());

        EmployeeLeave savedLeave = leaveRepository.save(leave);

        log.info("[{}] Leave allocation created successfully. allocationId={}, employeeId={}",
                LogTags.SERVICE,
                savedLeave.getId(),
                employee.getId());

        return mapToResponse(savedLeave);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeLeaveResponse getLeaveAllocation(Long id) {

        log.debug("[{}] Fetching leave allocation. allocationId={}",
                LogTags.SERVICE, id);

        return mapToResponse(findLeave(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLeaveResponse> getEmployeeLeaves(
            Long employeeId) {

        log.debug("[{}] Fetching employee leave allocations. employeeId={}",
                LogTags.SERVICE, employeeId);

        findEmployee(employeeId);

        List<EmployeeLeaveResponse> leaves =
                leaveRepository.findByEmployeeId(employeeId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        log.info("[{}] Employee leave allocations fetched. employeeId={}, count={}",
                LogTags.SERVICE, employeeId, leaves.size());

        return leaves;
    }

    @Override
    public EmployeeLeaveResponse updateLeaveAllocation(
            Long id, EmployeeLeaveRequest request) {

        log.info("[{}] Updating leave allocation. allocationId={}",
                LogTags.SERVICE, id);

        validateRequest(request);

        EmployeeLeave leave = findLeave(id);

        if (!leave.getEmployee().getId()
                .equals(request.employeeId())) {

            log.warn("[{}] Attempt to change employee for existing leave allocation. allocationId={}",
                    LogTags.SERVICE, id);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee cannot be changed for an existing leave allocation");
        }

        leaveRepository.findByEmployeeIdAndLeaveType(
                        request.employeeId(), request.leaveType())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {

                    log.warn("[{}] Duplicate leave allocation detected. employeeId={}, leaveType={}",
                            LogTags.SERVICE,
                            request.employeeId(),
                            request.leaveType());

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Leave allocation already exists for this leave type");
                });

        leave.setLeaveType(request.leaveType());
        leave.setTotalLeaves(request.totalLeaves());
        leave.setAppliedDate(request.appliedDate());

        EmployeeLeave updatedLeave = leaveRepository.save(leave);

        log.info("[{}] Leave allocation updated successfully. allocationId={}",
                LogTags.SERVICE, id);

        return mapToResponse(updatedLeave);
    }

    @Override
    public void deleteLeaveAllocation(Long id) {

        log.info("[{}] Deleting leave allocation. allocationId={}",
                LogTags.SERVICE, id);

        EmployeeLeave leave = findLeave(id);

        leaveRepository.delete(leave);

        log.info("[{}] Leave allocation deleted successfully. allocationId={}",
                LogTags.SERVICE, id);
    }

    private Employee findEmployee(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> {

                    log.warn("[{}] Employee not found. employeeId={}",
                            LogTags.DB_OP, employeeId);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Employee not found");
                });
    }

    private EmployeeLeave findLeave(Long id) {

        return leaveRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn("[{}] Leave allocation not found. allocationId={}",
                            LogTags.DB_OP, id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Leave allocation not found");
                });
    }

    private void validateRequest(EmployeeLeaveRequest request) {

        if (request == null
                || request.employeeId() == null
                || request.leaveType() == null
                || request.totalLeaves() == null
                || request.appliedDate() == null) {

            log.warn("[{}] Invalid leave allocation request",
                    LogTags.VALIDATION);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee ID, leave type, total leaves and applied date are required");
        }

        if (request.totalLeaves() < 0) {

            log.warn("[{}] Negative total leaves provided",
                    LogTags.VALIDATION);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total leaves cannot be negative");
        }
    }

    private EmployeeLeaveResponse mapToResponse(
            EmployeeLeave leave) {

        Employee employee = leave.getEmployee();

        return new EmployeeLeaveResponse(
                leave.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                leave.getLeaveType(),
                leave.getTotalLeaves(),
                leave.getAppliedDate()
        );
    }
}