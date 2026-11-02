package com.employee_managemet.employee_service.service;

import com.employee_managemet.employee_service.dto.DepartmentRequest;
import com.employee_managemet.employee_service.dto.DepartmentResponse;
import com.employee_managemet.employee_service.model.Department;
import com.employee_managemet.employee_service.repository.DepartmentRepository;
import com.employee_managemet.employee_service.service.impl.DepartmentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentRequest request;

    @BeforeEach
    void setUp() {
        request = new DepartmentRequest(
                "Engineering",
                "Engineering department"
        );
    }

    private Department createDepartment() {
        Department department = new Department();

        ReflectionTestUtils.setField(department, "id", 1L);

        department.setName("Engineering");
        department.setDescription("Engineering department");
        department.setActive(true);

        return department;
    }

    private void assertStatus(
            ResponseStatusException exception,
            HttpStatus expectedStatus) {

        assertEquals(
                expectedStatus.value(),
                exception.getStatusCode().value()
        );
    }

    // CREATE DEPARTMENT

    @Test
    void createDepartment_shouldCreateSuccessfully() {

        when(departmentRepository.findByNameIgnoreCase("Engineering"))
                .thenReturn(Optional.empty());

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentResponse response =
                departmentService.createDepartment(request);

        assertNotNull(response);
        assertEquals("Engineering", response.name());
        assertTrue(response.active());

        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_whenNameExists_shouldThrowConflict() {

        when(departmentRepository.findByNameIgnoreCase("Engineering"))
                .thenReturn(Optional.of(createDepartment()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.createDepartment(request)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(departmentRepository, never()).save(any());
    }

    // GET DEPARTMENT BY ID

    @Test
    void getDepartmentById_shouldReturnDepartment() {

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(createDepartment()));

        DepartmentResponse response =
                departmentService.getDepartmentById(1L);

        assertNotNull(response);
        assertEquals("Engineering", response.name());
    }

    @Test
    void getDepartmentById_whenNotFound_shouldThrowNotFound() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.getDepartmentById(99L)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    // GET ALL DEPARTMENTS

    @Test
    void getAllDepartments_shouldReturnDepartmentList() {

        Department firstDepartment = createDepartment();

        Department secondDepartment = new Department();
        ReflectionTestUtils.setField(secondDepartment, "id", 2L);
        secondDepartment.setName("Finance");
        secondDepartment.setDescription("Finance department");
        secondDepartment.setActive(true);

        when(departmentRepository.findAll())
                .thenReturn(List.of(
                        firstDepartment,
                        secondDepartment
                ));

        List<DepartmentResponse> response =
                departmentService.getAllDepartments();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Engineering", response.get(0).name());
        assertEquals("Finance", response.get(1).name());
    }

    @Test
    void getAllDepartments_whenNoDepartments_shouldReturnEmptyList() {

        when(departmentRepository.findAll())
                .thenReturn(List.of());

        List<DepartmentResponse> response =
                departmentService.getAllDepartments();

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    // UPDATE DEPARTMENT

    @Test
    void updateDepartment_shouldUpdateSuccessfully() {

        Department existingDepartment = createDepartment();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(existingDepartment));

        when(departmentRepository.findByNameIgnoreCase("Engineering"))
                .thenReturn(Optional.of(existingDepartment));

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentResponse response =
                departmentService.updateDepartment(1L, request);

        assertNotNull(response);
        assertEquals("Engineering", response.name());

        verify(departmentRepository).save(existingDepartment);
    }

    @Test
    void updateDepartment_whenDepartmentDoesNotExist_shouldThrowNotFound() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.updateDepartment(99L, request)
        );

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    @Test
    void updateDepartment_whenNameBelongsToAnotherDepartment_shouldThrowConflict() {

        Department existingDepartment = createDepartment();

        Department anotherDepartment = new Department();
        ReflectionTestUtils.setField(anotherDepartment, "id", 2L);
        anotherDepartment.setName("Engineering");
        anotherDepartment.setDescription("Another department");
        anotherDepartment.setActive(true);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(existingDepartment));

        when(departmentRepository.findByNameIgnoreCase("Engineering"))
                .thenReturn(Optional.of(anotherDepartment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.updateDepartment(1L, request)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void updateDepartment_whenInactive_shouldThrowConflict() {

        Department department = createDepartment();
        department.setActive(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.updateDepartment(1L, request)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(departmentRepository, never()).save(any());
    }

    // DEACTIVATE DEPARTMENT

    @Test
    void deactivateDepartment_shouldSetActiveFalse() {

        Department department = createDepartment();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        departmentService.deactivateDepartment(1L);

        assertFalse(department.getActive());

        verify(departmentRepository).save(department);
    }

    @Test
    void deactivateDepartment_whenAlreadyInactive_shouldThrowConflict() {

        Department department = createDepartment();
        department.setActive(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.deactivateDepartment(1L)
        );

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(departmentRepository, never()).save(any());
    }
}