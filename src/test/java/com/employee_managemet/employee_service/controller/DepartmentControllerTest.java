
package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.dto.DepartmentRequest;
import com.employee_managemet.employee_service.dto.DepartmentResponse;
import com.employee_managemet.employee_service.exception.GlobalExceptionHandler;
import com.employee_managemet.employee_service.service.DepartmentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@Import(GlobalExceptionHandler.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    private DepartmentResponse departmentResponse() {
        return new DepartmentResponse(
                1L,
                "Engineering",
                "Engineering department",
                true
        );
    }

    @Test
    void createDepartment_shouldReturn201() throws Exception {

        when(departmentService.createDepartment(
                any(DepartmentRequest.class)))
                .thenReturn(departmentResponse());

        String request = """
                {
                  "name": "Engineering",
                  "description": "Engineering department"
                }
                """;

        mockMvc.perform(post("/api/v1/departments/createdepartment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"));

        verify(departmentService).createDepartment(
                any(DepartmentRequest.class));
    }

    @Test
    void getDepartmentById_shouldReturn200() throws Exception {

        when(departmentService.getDepartmentById(1L))
                .thenReturn(departmentResponse());

        mockMvc.perform(
                        get("/api/v1/departments/getdepartmentbyid/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engineering"));

        verify(departmentService).getDepartmentById(1L);
    }

    @Test
    void getAllDepartments_shouldReturn200() throws Exception {

        when(departmentService.getAllDepartments())
                .thenReturn(List.of(departmentResponse()));

        mockMvc.perform(
                        get("/api/v1/departments/getalldepartments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Engineering"));

        verify(departmentService).getAllDepartments();
    }

    @Test
    void updateDepartment_shouldReturn200() throws Exception {

        when(departmentService.updateDepartment(
                eq(1L), any(DepartmentRequest.class)))
                .thenReturn(departmentResponse());

        String request = """
                {
                  "name": "Engineering",
                  "description": "Updated engineering department"
                }
                """;

        mockMvc.perform(put(
                        "/api/v1/departments/updatedepartment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(departmentService).updateDepartment(
                eq(1L), any(DepartmentRequest.class));
    }

    @Test
    void deactivateDepartment_shouldReturn204() throws Exception {

        doNothing().when(departmentService).deactivateDepartment(1L);

        mockMvc.perform(delete(
                        "/api/v1/departments/deactivatedepartment/1"))
                .andExpect(status().isNoContent());

        verify(departmentService).deactivateDepartment(1L);
    }

    @Test
    void getDepartmentById_whenNotFound_shouldReturn404() throws Exception {

        when(departmentService.getDepartmentById(99L))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Department not found"));

        mockMvc.perform(
                        get("/api/v1/departments/getdepartmentbyid/99"))
                .andExpect(status().isNotFound());
    }
}
