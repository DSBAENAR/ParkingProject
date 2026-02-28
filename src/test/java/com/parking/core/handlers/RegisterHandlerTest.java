package com.parking.core.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.service.RegisterService;

@WebMvcTest(RegisterHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class RegisterHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterService registerService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET /registers - should return all registers")
    void shouldReturnAllRegisters() throws Exception {
        Vehicle v = new Vehicle("ABC123", VehicleType.NON_RESIDENT);
        Register r = new Register(v);
        r.setId(1L);
        r.setEntrydate(LocalDateTime.now());
        when(registerService.getAllRegisters()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/parking/registers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registers.length()").value(1));
    }

    @Test
    @DisplayName("POST /register - should register vehicle entrance")
    void shouldRegisterVehicle() throws Exception {
        Vehicle v = new Vehicle("ABC123", VehicleType.NON_RESIDENT);
        Register r = new Register(v);
        r.setId(1L);
        r.setEntrydate(LocalDateTime.now());
        when(registerService.registerVehicleEntrance(any(Vehicle.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/parking/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123", "type": "NON_RESIDENT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Register created successfully"))
                .andExpect(jsonPath("$.register").exists());
    }

    @Test
    @DisplayName("POST /register - should return 400 for validation errors (blank id)")
    void shouldReturn400ForBlankId() throws Exception {
        mockMvc.perform(post("/api/v1/parking/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "", "type": "NON_RESIDENT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.id").exists());
    }

    @Test
    @DisplayName("POST /leave - should process vehicle departure")
    void shouldProcessDeparture() throws Exception {
        Vehicle v = new Vehicle("ABC123", VehicleType.NON_RESIDENT);
        Register r = new Register(v);
        r.setId(1L);
        r.setEntrydate(LocalDateTime.now().minusMinutes(60));
        r.setExitdate(LocalDateTime.now());
        r.setMinutes(60);
        when(registerService.leaveVehicle(any(Vehicle.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/parking/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123", "type": "NON_RESIDENT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Register updated successfully"))
                .andExpect(jsonPath("$.register.minutes").value(60));
    }

    @Test
    @DisplayName("POST /leave - should return 400 when no active register")
    void shouldReturn400WhenNoActiveRegister() throws Exception {
        when(registerService.leaveVehicle(any(Vehicle.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active register found for vehicle"));

        mockMvc.perform(post("/api/v1/parking/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123", "type": "NON_RESIDENT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No active register found for vehicle"));
    }
}
