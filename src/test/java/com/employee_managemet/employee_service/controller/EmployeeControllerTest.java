
package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.dto.EmployeeRequest;
import com.employee_managemet.employee_service.dto.EmployeeResponse;
import com.employee_managemet.employee_service.enums.*;
import com.employee_managemet.employee_service.service.EmployeeService;
import com.employee_managemet.employee_service.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    private EmployeeResponse employeeResponse() {
        return new EmployeeResponse(
                1L,
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
                "Engineering",
                null
        );
    }

    @Test
    void createEmployee_shouldReturn201() throws Exception {

        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenReturn(employeeResponse());

        String request = """
                {
                   "employeeCode": "EMP001",
                   "name": "John Doe",
                   "gender": "MALE",
                   "employmentType": "PERMANENT",
                   "employmentStatus": "ACTIVE",
                   "joiningDate": "2025-01-10",
                   "mobileNumber": "9876543210",
                   "email": "john@example.com",
                   "position": "Software Engineer",
                   "departmentId": 1,
                   "address": {
                     "addressLine1": "123 Main Street",
                     "addressLine2": "Near City Centre",
                     "city": "Bengaluru",
                     "state": "Karnataka",
                     "country": "India",
                     "postalCode": "560001"
                   }
                 }
                """;

        mockMvc.perform(post("/api/v1/employees/createemployee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));

        verify(employeeService).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    void getEmployeeById_shouldReturn200() throws Exception {

        when(employeeService.getEmployeeById(1L))
                .thenReturn(employeeResponse());

        mockMvc.perform(get("/api/v1/employees/getemployeebyid/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    void getEmployeeByCode_shouldReturn200() throws Exception {

        when(employeeService.getEmployeeByCode("EMP001"))
                .thenReturn(employeeResponse());

        mockMvc.perform(
                        get("/api/v1/employees/getemployeebycode/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));

        verify(employeeService).getEmployeeByCode("EMP001");
    }

    @Test
    void getAllEmployees_shouldReturn200() throws Exception {

        Page<EmployeeResponse> page =
                new PageImpl<>(List.of(employeeResponse()));

        when(employeeService.getAllEmployees(any()))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/employees/getallemployees")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].employeeCode")
                        .value("EMP001"));

        verify(employeeService).getAllEmployees(any());
    }

    @Test
    void updateEmployee_shouldReturn200() throws Exception {

        when(employeeService.updateEmployee(
                eq(1L), any(EmployeeRequest.class)))
                .thenReturn(employeeResponse());

        String request = """
                {
                   "employeeCode": "EMP001",
                   "name": "John Doe",
                   "gender": "MALE",
                   "employmentType": "PERMANENT",
                   "employmentStatus": "ACTIVE",
                   "joiningDate": "2025-01-10",
                   "mobileNumber": "9876543210",
                   "email": "john@example.com",
                   "position": "Senior Software Engineer",
                   "departmentId": 1,
                   "address": {
                     "addressLine1": "123 Main Street",
                     "addressLine2": "Near City Centre",
                     "city": "Bengaluru",
                     "state": "Karnataka",
                     "country": "India",
                     "postalCode": "560001"
                   }
                 }
                """;

        mockMvc.perform(put("/api/v1/employees/updateemployee/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(employeeService).updateEmployee(
                eq(1L), any(EmployeeRequest.class));
    }

    @Test
    void deactivateEmployee_shouldReturn204() throws Exception {

        doNothing().when(employeeService).deactivateEmployee(1L);

        mockMvc.perform(
                        delete("/api/v1/employees/deactivateemployee/1"))
                .andExpect(status().isNoContent());

        verify(employeeService).deactivateEmployee(1L);
    }

    @Test
    void getEmployeeById_whenNotFound_shouldReturn404() throws Exception {

        when(employeeService.getEmployeeById(99L))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Employee not found"));

        mockMvc.perform(get("/api/v1/employees/getemployeebyid/99"))
                .andExpect(status().isNotFound());
    }
}
