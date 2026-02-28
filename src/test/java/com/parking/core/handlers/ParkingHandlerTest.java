package com.parking.core.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

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
import com.parking.core.model.Vehicle;
import com.parking.core.service.ParkingService;

@WebMvcTest(ParkingHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ParkingHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingService parkingService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET / - should return all vehicles")
    void shouldReturnAllVehicles() throws Exception {
        Vehicle v1 = new Vehicle("ABC123", VehicleType.NON_RESIDENT);
        Vehicle v2 = new Vehicle("DEF456", VehicleType.RESIDENT);
        when(parkingService.getAllVehicles()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/v1/parking/vehicles/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicles.length()").value(2))
                .andExpect(jsonPath("$.vehicles[0].id").value("ABC123"));
    }

    @Test
    @DisplayName("POST / - should save new vehicle")
    void shouldSaveVehicle() throws Exception {
        Vehicle saved = new Vehicle("ABC123", VehicleType.NON_RESIDENT);
        when(parkingService.saveVehicle(any(Vehicle.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/parking/vehicles/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123", "type": "NON_RESIDENT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Vehicle created successfully"))
                .andExpect(jsonPath("$.vehicle.id").value("ABC123"));
    }

    @Test
    @DisplayName("POST / - should return 400 for missing type")
    void shouldReturn400ForMissingType() throws Exception {
        mockMvc.perform(post("/api/v1/parking/vehicles/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.type").exists());
    }

    @Test
    @DisplayName("POST / - should return 400 when vehicle already exists")
    void shouldReturn400WhenVehicleExists() throws Exception {
        when(parkingService.saveVehicle(any(Vehicle.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle already exists"));

        mockMvc.perform(post("/api/v1/parking/vehicles/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123", "type": "NON_RESIDENT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vehicle already exists"));
    }

    @Test
    @DisplayName("POST /pay - should calculate payment")
    void shouldCalculatePayment() throws Exception {
        when(parkingService.calculatePayment(any(Vehicle.class))).thenReturn(30.0);

        mockMvc.perform(post("/api/v1/parking/vehicles/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123", "type": "NON_RESIDENT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(30.0));
    }

    @Test
    @DisplayName("POST /startsMonth - should reset month")
    void shouldResetMonth() throws Exception {
        when(parkingService.monthStarts()).thenReturn(Map.of(
                "message", "Deleted all official registers",
                "deletedCount", 5));

        mockMvc.perform(post("/api/v1/parking/vehicles/startsMonth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(5));
    }

    @Test
    @DisplayName("PUT /{id} - should update vehicle type")
    void shouldUpdateVehicle() throws Exception {
        Vehicle updated = new Vehicle("ABC123", VehicleType.RESIDENT);
        when(parkingService.updateVehicle(eq("ABC123"), any(Vehicle.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/parking/vehicles/ABC123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "ABC123", "type": "RESIDENT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle updated successfully"))
                .andExpect(jsonPath("$.vehicle.type").value("RESIDENT"));
    }

    @Test
    @DisplayName("PUT /{id} - should return 404 when vehicle not found")
    void shouldReturn404OnUpdateNotFound() throws Exception {
        when(parkingService.updateVehicle(eq("UNKNOWN"), any(Vehicle.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle does not exist"));

        mockMvc.perform(put("/api/v1/parking/vehicles/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "UNKNOWN", "type": "OFICIAL"}
                                """))
                .andExpect(status().isNotFound());
    }
}
