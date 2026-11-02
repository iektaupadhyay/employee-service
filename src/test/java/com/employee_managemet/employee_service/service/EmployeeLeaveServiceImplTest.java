package com.employee_managemet.employee_service.service;

import com.employee_managemet.employee_service.dto.EmployeeLeaveRequest;
import com.employee_managemet.employee_service.dto.EmployeeLeaveResponse;
import com.employee_managemet.employee_service.enums.LeaveType;
import com.employee_managemet.employee_service.model.Employee;
import com.employee_managemet.employee_service.model.EmployeeLeave;
import com.employee_managemet.employee_service.repository.EmployeeLeaveRepository;
import com.employee_managemet.employee_service.repository.EmployeeRepository;
import com.employee_managemet.employee_service.service.impl.EmployeeLeaveServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeLeaveServiceImplTest {

    @Mock
    private EmployeeLeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeLeaveServiceImpl leaveService;

    private Employee employee;
    private EmployeeLeaveRequest request;

    @BeforeEach
    void setUp() {

        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", 1L);

        employee.setEmployeeCode("EMP001");
        employee.setName("John Doe");

        request = new EmployeeLeaveRequest(
                1L,
                LeaveType.CASUAL,
                12,
                LocalDate.of(2026, 1, 1)
        );
    }

    private EmployeeLeave createLeaveAllocation() {

        EmployeeLeave leave = new EmployeeLeave();

        ReflectionTestUtils.setField(leave, "id", 1L);

        leave.setEmployee(employee);
        leave.setLeaveType(LeaveType.CASUAL);
        leave.setTotalLeaves(12);
        leave.setAppliedDate(LocalDate.of(2026, 1, 1));

        return leave;
    }

    private void assertStatus(
            ResponseStatusException exception,
            HttpStatus expectedStatus) {

        assertEquals(
                expectedStatus.value(),
                exception.getStatusCode().value()
        );
    }

    // CREATE LEAVE ALLOCATION

    @Test
    void createLeaveAllocation_shouldCreateSuccessfully() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.existsByEmployeeIdAndLeaveType(
                1L, LeaveType.CASUAL))
                .thenReturn(false);

        when(leaveRepository.save(any(EmployeeLeave.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeLeaveResponse response =
                leaveService.createLeaveAllocation(request);

        assertNotNull(response);
        assertEquals("EMP001", response.employeeCode());
        assertEquals(LeaveType.CASUAL, response.leaveType());
        assertEquals(12, response.totalLeaves());

        verify(leaveRepository).save(any(EmployeeLeave.class));
    }

    @Test
    void createLeaveAllocation_whenEmployeeNotFound_shouldThrowNotFound() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.createLeaveAllocation(request)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);

        verify(leaveRepository, never()).save(any());
    }

    @Test
    void createLeaveAllocation_whenAllocationExists_shouldThrowConflict() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.existsByEmployeeIdAndLeaveType(
                1L, LeaveType.CASUAL))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.createLeaveAllocation(request)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(leaveRepository, never()).save(any());
    }

    @Test
    void createLeaveAllocation_whenRequestIsNull_shouldThrowBadRequest() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.createLeaveAllocation(null)
        );

        assertStatus(exception, HttpStatus.BAD_REQUEST);
    }

    @Test
    void createLeaveAllocation_whenTotalLeavesIsNegative_shouldThrowBadRequest() {

        EmployeeLeaveRequest invalidRequest =
                new EmployeeLeaveRequest(
                        1L,
                        LeaveType.CASUAL,
                        -5,
                        LocalDate.of(2026, 1, 1)
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.createLeaveAllocation(invalidRequest)
        );

        assertStatus(exception, HttpStatus.BAD_REQUEST);
    }

    // GET LEAVE ALLOCATION

    @Test
    void getLeaveAllocation_shouldReturnAllocation() {

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(createLeaveAllocation()));

        EmployeeLeaveResponse response =
                leaveService.getLeaveAllocation(1L);

        assertNotNull(response);
        assertEquals(LeaveType.CASUAL, response.leaveType());
        assertEquals(12, response.totalLeaves());
    }

    @Test
    void getLeaveAllocation_whenNotFound_shouldThrowNotFound() {

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.getLeaveAllocation(99L)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    // GET EMPLOYEE LEAVES

    @Test
    void getEmployeeLeaves_shouldReturnEmployeeAllocations() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.findByEmployeeId(1L))
                .thenReturn(List.of(createLeaveAllocation()));

        List<EmployeeLeaveResponse> response =
                leaveService.getEmployeeLeaves(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(LeaveType.CASUAL, response.get(0).leaveType());
    }

    @Test
    void getEmployeeLeaves_whenEmployeeNotFound_shouldThrowNotFound() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.getEmployeeLeaves(99L)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);

        verify(leaveRepository, never()).findByEmployeeId(any());
    }

    @Test
    void getEmployeeLeaves_whenNoAllocations_shouldReturnEmptyList() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.findByEmployeeId(1L))
                .thenReturn(List.of());

        List<EmployeeLeaveResponse> response =
                leaveService.getEmployeeLeaves(1L);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    // UPDATE LEAVE ALLOCATION

    @Test
    void updateLeaveAllocation_shouldUpdateSuccessfully() {

        EmployeeLeave existingLeave = createLeaveAllocation();

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(existingLeave));

        when(leaveRepository.save(any(EmployeeLeave.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeLeaveRequest updatedRequest =
                new EmployeeLeaveRequest(
                        1L,
                        LeaveType.CASUAL,
                        15,
                        LocalDate.of(2026, 2, 1)
                );

        EmployeeLeaveResponse response =
                leaveService.updateLeaveAllocation(1L, updatedRequest);

        assertNotNull(response);
        assertEquals(15, response.totalLeaves());
        assertEquals(LocalDate.of(2026, 2, 1), response.appliedDate());
        assertEquals(LeaveType.CASUAL, response.leaveType());

        verify(leaveRepository).save(existingLeave);
    }

    @Test
    void updateLeaveAllocation_whenAllocationNotFound_shouldThrowNotFound() {

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.updateLeaveAllocation(99L, request)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    @Test
    void updateLeaveAllocation_whenEmployeeIsChanged_shouldThrowBadRequest() {

        EmployeeLeave existingLeave = createLeaveAllocation();

        EmployeeLeaveRequest changedEmployeeRequest =
                new EmployeeLeaveRequest(
                        2L,
                        LeaveType.CASUAL,
                        12,
                        LocalDate.of(2026, 1, 1)
                );

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(existingLeave));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.updateLeaveAllocation(
                        1L,
                        changedEmployeeRequest
                )
        );

        assertStatus(exception, HttpStatus.BAD_REQUEST);

        verify(employeeRepository, never()).findById(any());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void updateLeaveAllocation_whenDuplicateAllocationExists_shouldThrowConflict() {

        EmployeeLeave existingLeave = createLeaveAllocation();

        EmployeeLeave anotherLeave = createLeaveAllocation();
        ReflectionTestUtils.setField(anotherLeave, "id", 2L);
        anotherLeave.setLeaveType(LeaveType.SICK);

        EmployeeLeaveRequest updatedRequest =
                new EmployeeLeaveRequest(
                        1L,
                        LeaveType.SICK,
                        10,
                        LocalDate.of(2026, 2, 1)
                );

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(existingLeave));

        when(leaveRepository.findByEmployeeIdAndLeaveType(
                1L, LeaveType.SICK))
                .thenReturn(Optional.of(anotherLeave));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.updateLeaveAllocation(1L, updatedRequest)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(leaveRepository, never()).save(any(EmployeeLeave.class));
    }

    // DELETE LEAVE ALLOCATION

    @Test
    void deleteLeaveAllocation_shouldDeleteSuccessfully() {

        EmployeeLeave leave = createLeaveAllocation();

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        doNothing().when(leaveRepository).delete(leave);

        assertDoesNotThrow(
                () -> leaveService.deleteLeaveAllocation(1L)
        );

        verify(leaveRepository).delete(leave);
    }

    @Test
    void deleteLeaveAllocation_whenNotFound_shouldThrowNotFound() {

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> leaveService.deleteLeaveAllocation(99L)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);

        verify(leaveRepository, never()).delete(any());
    }
}