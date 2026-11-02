package com.employee_managemet.employee_service.repository;

import com.employee_managemet.employee_service.model.EmployeeLeave;
import com.employee_managemet.employee_service.enums.LeaveType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeLeaveRepository
        extends JpaRepository<EmployeeLeave, Long> {

    List<EmployeeLeave> findByEmployeeId(Long employeeId);

    Optional<EmployeeLeave> findByEmployeeIdAndLeaveType(
            Long employeeId, LeaveType leaveType);

    boolean existsByEmployeeIdAndLeaveType(
            Long employeeId, LeaveType leaveType);
}