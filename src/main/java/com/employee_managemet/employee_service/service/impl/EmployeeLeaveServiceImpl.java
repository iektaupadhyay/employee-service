package com.employee_managemet.employee_service.service.impl;

import com.employee_managemet.employee_service.dto.EmployeeLeaveRequest;
import com.employee_managemet.employee_service.dto.EmployeeLeaveResponse;
import com.employee_managemet.employee_service.model.Employee;
import com.employee_managemet.employee_service.model.EmployeeLeave;
import com.employee_managemet.employee_service.repository.EmployeeLeaveRepository;
import com.employee_managemet.employee_service.repository.EmployeeRepository;
import com.employee_managemet.employee_service.service.EmployeeLeaveService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

        validateRequest(request);

        Employee employee = findEmployee(request.employeeId());

        if (leaveRepository.existsByEmployeeIdAndLeaveType(
                request.employeeId(), request.leaveType())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leave allocation already exists for this employee and leave type");
        }

        EmployeeLeave leave = new EmployeeLeave();

        leave.setEmployee(employee);
        leave.setLeaveType(request.leaveType());
        leave.setTotalLeaves(request.totalLeaves());
        leave.setAppliedDate(request.appliedDate());

        return mapToResponse(leaveRepository.save(leave));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeLeaveResponse getLeaveAllocation(Long id) {

        return mapToResponse(findLeave(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLeaveResponse> getEmployeeLeaves(
            Long employeeId) {

        findEmployee(employeeId);

        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EmployeeLeaveResponse updateLeaveAllocation(
            Long id, EmployeeLeaveRequest request) {

        validateRequest(request);

        EmployeeLeave leave = findLeave(id);

        if (!leave.getEmployee().getId()
                .equals(request.employeeId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee cannot be changed for an existing leave allocation");
        }

        leaveRepository.findByEmployeeIdAndLeaveType(
                        request.employeeId(), request.leaveType())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Leave allocation already exists for this leave type");
                });

        leave.setLeaveType(request.leaveType());
        leave.setTotalLeaves(request.totalLeaves());
        leave.setAppliedDate(request.appliedDate());

        return mapToResponse(leaveRepository.save(leave));
    }

    @Override
    public void deleteLeaveAllocation(Long id) {

        EmployeeLeave leave = findLeave(id);

        leaveRepository.delete(leave);
    }

    private Employee findEmployee(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found"));
    }

    private EmployeeLeave findLeave(Long id) {

        return leaveRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Leave allocation not found"));
    }

    private void validateRequest(EmployeeLeaveRequest request) {

        if (request == null
                || request.employeeId() == null
                || request.leaveType() == null
                || request.totalLeaves() == null
                || request.appliedDate() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee ID, leave type, total leaves and applied date are required");
        }

        if (request.totalLeaves() < 0) {
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