
package com.employee_managemet.employee_service.controller;

import com.employee_managemet.employee_service.dto.EmployeeLeaveRequest;
import com.employee_managemet.employee_service.dto.EmployeeLeaveResponse;
import com.employee_managemet.employee_service.exception.GlobalExceptionHandler;
import com.employee_managemet.employee_service.enums.LeaveType;
import com.employee_managemet.employee_service.service.EmployeeLeaveService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeLeaveController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeLeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeLeaveService leaveService;

    private EmployeeLeaveResponse leaveResponse() {
        return new EmployeeLeaveResponse(
                1L,
                1L,
                "EMP001",
                "John Doe",
                LeaveType.CASUAL,
                12,
                LocalDate.of(2026, 1, 1)
        );
    }

    @Test
    void createAllocation_shouldReturn201() throws Exception {

        when(leaveService.createLeaveAllocation(
                any(EmployeeLeaveRequest.class)))
                .thenReturn(leaveResponse());

        String request = """
                {
                  "employeeId": 1,
                  "leaveType": "CASUAL",
                  "totalLeaves": 12,
                  "appliedDate": "2026-01-01"
                }
                """;

        mockMvc.perform(post(
                        "/api/v1/employee-leaves/createleaveallocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.leaveType").value("CASUAL"));

        verify(leaveService).createLeaveAllocation(
                any(EmployeeLeaveRequest.class));
    }

    @Test
    void getAllocation_shouldReturn200() throws Exception {

        when(leaveService.getLeaveAllocation(1L))
                .thenReturn(leaveResponse());

        mockMvc.perform(get(
                        "/api/v1/employee-leaves/getleaveallocation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(leaveService).getLeaveAllocation(1L);
    }

    @Test
    void getEmployeeLeaves_shouldReturn200() throws Exception {

        when(leaveService.getEmployeeLeaves(1L))
                .thenReturn(List.of(leaveResponse()));

        mockMvc.perform(get(
                        "/api/v1/employee-leaves/getemployeeleaves/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(1))
                .andExpect(jsonPath("$[0].leaveType").value("CASUAL"));

        verify(leaveService).getEmployeeLeaves(1L);
    }

    @Test
    void updateAllocation_shouldReturn200() throws Exception {

        when(leaveService.updateLeaveAllocation(
                eq(1L), any(EmployeeLeaveRequest.class)))
                .thenReturn(leaveResponse());

        String request = """
                {
                  "employeeId": 1,
                  "leaveType": "CASUAL",
                  "totalLeaves": 15,
                  "appliedDate": "2026-01-01"
                }
                """;

        mockMvc.perform(put(
                        "/api/v1/employee-leaves/updateleaveallocation/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(leaveService).updateLeaveAllocation(
                eq(1L), any(EmployeeLeaveRequest.class));
    }

    @Test
    void deleteAllocation_shouldReturn204() throws Exception {

        doNothing().when(leaveService).deleteLeaveAllocation(1L);

        mockMvc.perform(delete(
                        "/api/v1/employee-leaves/deleteleaveallocation/1"))
                .andExpect(status().isNoContent());

        verify(leaveService).deleteLeaveAllocation(1L);
    }

    @Test
    void getAllocation_whenNotFound_shouldReturn404() throws Exception {

        when(leaveService.getLeaveAllocation(99L))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Leave allocation not found"));

        mockMvc.perform(get(
                        "/api/v1/employee-leaves/getleaveallocation/99"))
                .andExpect(status().isNotFound());
    }
}
