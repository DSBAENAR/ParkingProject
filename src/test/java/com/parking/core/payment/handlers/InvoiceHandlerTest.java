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
import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.InvoiceRequest;
import com.parking.core.payment.Requests.UserTax;
import com.parking.core.payment.response.InvoiceResponse;
import com.parking.core.payment.services.InvoiceService;

import java.math.BigDecimal;

@WebMvcTest(InvoiceHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /invoice - success")
    void addInvoice_success() throws Exception {
        UserTax tax = new UserTax(true, "CO", BigDecimal.valueOf(19.0), "DC");
        InvoiceResponse response = new InvoiceResponse("inv_123", Currencies.COP, "cus_123",
                tax, Map.of());
        when(invoiceService.createAnInvoceForAUser(any(InvoiceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/invoices/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customer":"cus_123","currency":"COP","product":{"hours":2,"price":25000}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invoice created correctly"))
                .andExpect(jsonPath("$.invoice.id").value("inv_123"));
    }

    @Test
    @DisplayName("POST /invoice - service error")
    void addInvoice_error() throws Exception {
        when(invoiceService.createAnInvoceForAUser(any(InvoiceRequest.class)))
                .thenThrow(new RuntimeException("Stripe error"));

        mockMvc.perform(post("/api/invoices/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customer":"cus_123","currency":"COP","product":{"hours":2,"price":25000}}
                                """))
                .andExpect(status().isInternalServerError());
    }
}
