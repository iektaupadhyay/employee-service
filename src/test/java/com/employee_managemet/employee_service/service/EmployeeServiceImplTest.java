package com.employee_managemet.employee_service.service;

import com.employee_managemet.employee_service.dto.EmployeeRequest;
import com.employee_managemet.employee_service.dto.EmployeeResponse;
import com.employee_managemet.employee_service.enums.EmploymentStatus;
import com.employee_managemet.employee_service.enums.EmploymentType;
import com.employee_managemet.employee_service.enums.Gender;
import com.employee_managemet.employee_service.model.Department;
import com.employee_managemet.employee_service.model.Employee;
import com.employee_managemet.employee_service.model.EmployeeAddress;
import com.employee_managemet.employee_service.repository.DepartmentRepository;
import com.employee_managemet.employee_service.repository.EmployeeRepository;
import com.employee_managemet.employee_service.service.impl.EmployeeServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequest request;
    private Department department;

    @BeforeEach
    void setUp() {

        EmployeeAddress address = new EmployeeAddress();

        request = new EmployeeRequest(
                "EMP001",
                "John Doe",
                Gender.MALE,
                EmploymentType.PERMANENT,
                EmploymentStatus.ACTIVE,
                LocalDate.of(2025, 1, 10),
                "9876543210",
                "john@example.com",
                "Software Engineer",
                1L,
                address
        );

        department = new Department();
        ReflectionTestUtils.setField(department, "id", 1L);

        department.setName("Engineering");
        department.setDescription("Engineering department");
        department.setActive(true);
    }

    private Employee createEmployee() {

        Employee employee = new Employee();

        ReflectionTestUtils.setField(employee, "id", 1L);

        employee.setEmployeeCode("EMP001");
        employee.setName("John Doe");
        employee.setGender(Gender.MALE);
        employee.setEmploymentType(EmploymentType.PERMANENT);
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setJoiningDate(LocalDate.of(2025, 1, 10));
        employee.setMobileNumber("9876543210");
        employee.setEmail("john@example.com");
        employee.setPosition("Software Engineer");
        employee.setDepartment(department);
        employee.setAddress(request.address());

        return employee;
    }

    private void mockValidCreateRequest() {

        when(employeeRepository.existsByEmployeeCode("EMP001"))
                .thenReturn(false);

        when(employeeRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));
    }

    private void assertStatus(
            ResponseStatusException exception,
            HttpStatus expectedStatus) {

        assertEquals(
                expectedStatus.value(),
                exception.getStatusCode().value()
        );
    }

    // CREATE EMPLOYEE

    @Test
    void createEmployee_shouldCreateSuccessfully() {

        mockValidCreateRequest();

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse response =
                employeeService.createEmployee(request);

        assertNotNull(response);
        assertEquals("EMP001", response.employeeCode());
        assertEquals("John Doe", response.name());

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_whenEmployeeCodeExists_shouldThrowConflict() {

        when(employeeRepository.existsByEmployeeCode("EMP001"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.createEmployee(request)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_whenEmailExists_shouldThrowConflict() {

        when(employeeRepository.existsByEmployeeCode("EMP001"))
                .thenReturn(false);

        when(employeeRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.createEmployee(request)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_whenDepartmentDoesNotExist_shouldThrowNotFound() {

        when(employeeRepository.existsByEmployeeCode("EMP001"))
                .thenReturn(false);

        when(employeeRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.createEmployee(request)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_whenDepartmentIsInactive_shouldThrowBadRequest() {

        department.setActive(false);

        when(employeeRepository.existsByEmployeeCode("EMP001"))
                .thenReturn(false);

        when(employeeRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.createEmployee(request)
        );

        assertStatus(exception, HttpStatus.BAD_REQUEST);

        verify(employeeRepository, never()).save(any());
    }

    // GET EMPLOYEE BY ID

    @Test
    void getEmployeeById_shouldReturnEmployee() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(createEmployee()));

        EmployeeResponse response =
                employeeService.getEmployeeById(1L);

        assertNotNull(response);
        assertEquals("EMP001", response.employeeCode());
    }

    @Test
    void getEmployeeById_whenEmployeeDoesNotExist_shouldThrowNotFound() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.getEmployeeById(99L)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    // GET EMPLOYEE BY CODE

    @Test
    void getEmployeeByCode_shouldReturnEmployee() {

        when(employeeRepository.findByEmployeeCode("EMP001"))
                .thenReturn(Optional.of(createEmployee()));

        EmployeeResponse response =
                employeeService.getEmployeeByCode("EMP001");

        assertNotNull(response);
        assertEquals("EMP001", response.employeeCode());
    }

    @Test
    void getEmployeeByCode_whenNotFound_shouldThrowNotFound() {

        when(employeeRepository.findByEmployeeCode("UNKNOWN"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.getEmployeeByCode("UNKNOWN")
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    // GET ALL EMPLOYEES

    @Test
    void getAllEmployees_shouldReturnPagedEmployees() {

        PageRequest pageable = PageRequest.of(0, 20);

        Page<Employee> employeePage =
                new PageImpl<>(List.of(createEmployee()));

        when(employeeRepository.findAll(pageable))
                .thenReturn(employeePage);

        Page<EmployeeResponse> response =
                employeeService.getAllEmployees(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("EMP001", response.getContent().get(0).employeeCode());
    }

    // UPDATE EMPLOYEE

    @Test
    void updateEmployee_shouldUpdateSuccessfully() {

        Employee existingEmployee = createEmployee();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.findByEmployeeCode("EMP001"))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(existingEmployee));

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse response =
                employeeService.updateEmployee(1L, request);

        assertNotNull(response);
        assertEquals("EMP001", response.employeeCode());

        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void updateEmployee_whenEmployeeDoesNotExist_shouldThrowNotFound() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.updateEmployee(99L, request)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);

        verify(employeeRepository, never()).save(any());
    }

    // DEACTIVATE EMPLOYEE

    @Test
    void deactivateEmployee_shouldSetStatusToInactive() {

        Employee employee = createEmployee();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.deactivateEmployee(1L);

        assertEquals(
                EmploymentStatus.INACTIVE,
                employee.getEmploymentStatus()
        );

        verify(employeeRepository).save(employee);
    }

    @Test
    void deactivateEmployee_whenAlreadyInactive_shouldThrowConflict() {

        Employee employee = createEmployee();
        employee.setEmploymentStatus(EmploymentStatus.INACTIVE);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> employeeService.deactivateEmployee(1L)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(employeeRepository, never()).save(any());
    }
}