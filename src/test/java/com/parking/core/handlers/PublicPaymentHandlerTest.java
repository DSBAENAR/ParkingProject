package com.parking.core.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.payment.Requests.PaymentRequest;
import com.parking.core.payment.services.PaymentService;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.service.ParkingService;

@WebMvcTest(PublicPaymentHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicPaymentHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterRepository registerRepository;

    @MockitoBean
    private ParkingService parkingService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    private Register buildRegister(boolean withExit) {
        Vehicle vehicle = new Vehicle("ABC-123", VehicleType.NON_RESIDENT);
        Register register = new Register(vehicle);
        register.setId(1L);
        register.setEntrydate(LocalDateTime.of(2025, 1, 15, 10, 0));
        if (withExit) {
            register.setExitdate(LocalDateTime.of(2025, 1, 15, 12, 0));
            register.setMinutes(120);
        }
        return register;
    }

    @Test
    @DisplayName("GET /{registerId} - success")
    void getPaymentDetails_success() throws Exception {
        Register register = buildRegister(true);
        when(registerRepository.findById(1L)).thenReturn(Optional.of(register));
        when(parkingService.calculatePaymentForRegister(register)).thenReturn(50.0);

        mockMvc.perform(get("/api/v1/public/pay/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registerId").value(1))
                .andExpect(jsonPath("$.plate").value("ABC-123"))
                .andExpect(jsonPath("$.amount").value(50.0));
    }

    @Test
    @DisplayName("GET /{registerId} - not found")
    void getPaymentDetails_notFound() throws Exception {
        when(registerRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/public/pay/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{registerId} - vehicle not exited")
    void getPaymentDetails_notExited() throws Exception {
        Register register = buildRegister(false);
        when(registerRepository.findById(1L)).thenReturn(Optional.of(register));

        mockMvc.perform(get("/api/v1/public/pay/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /{registerId}/create-intent - success")
    void createIntent_success() throws Exception {
        Register register = buildRegister(true);
        when(registerRepository.findById(1L)).thenReturn(Optional.of(register));
        when(parkingService.calculatePaymentForRegister(register)).thenReturn(50.0);
        when(paymentService.createPaymentIntent(any(PaymentRequest.class)))
                .thenReturn(Map.of("clientSecret", "cs_123", "paymentIntentId", "pi_123"));

        mockMvc.perform(post("/api/v1/public/pay/1/create-intent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret").value("cs_123"));
    }

    @Test
    @DisplayName("POST /{registerId}/create-intent - not found")
    void createIntent_notFound() throws Exception {
        when(registerRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/public/pay/999/create-intent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{registerId}/create-intent - not exited")
    void createIntent_notExited() throws Exception {
        Register register = buildRegister(false);
        when(registerRepository.findById(1L)).thenReturn(Optional.of(register));

        mockMvc.perform(post("/api/v1/public/pay/1/create-intent"))
                .andExpect(status().isBadRequest());
    }
}
