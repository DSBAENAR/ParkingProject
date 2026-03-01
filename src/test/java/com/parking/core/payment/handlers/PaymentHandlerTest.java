package com.parking.core.payment.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.payment.Requests.PaymentRequest;
import com.parking.core.payment.services.PaymentService;

@WebMvcTest(PaymentHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /create-intent - success")
    void createPaymentIntent_success() throws Exception {
        when(paymentService.createPaymentIntent(any(PaymentRequest.class)))
                .thenReturn(Map.of("clientSecret", "cs_123", "paymentIntentId", "pi_123"));

        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":5000,"currency":"usd","vehicleId":"ABC-123","description":"Parking fee"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret").value("cs_123"))
                .andExpect(jsonPath("$.paymentIntentId").value("pi_123"));
    }

    @Test
    @DisplayName("POST /create-intent - service error")
    void createPaymentIntent_error() throws Exception {
        when(paymentService.createPaymentIntent(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException("Stripe error"));

        mockMvc.perform(post("/api/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":5000,"currency":"usd","vehicleId":"ABC-123"}
                                """))
                .andExpect(status().isInternalServerError());
    }
}
