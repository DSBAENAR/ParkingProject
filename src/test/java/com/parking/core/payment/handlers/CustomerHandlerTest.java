package com.parking.core.payment.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;

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
import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.Requests.UserAddress;
import com.parking.core.payment.response.CustomerResponse;
import com.parking.core.payment.services.CustomerService;

@WebMvcTest(CustomerHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /customer - success")
    void addCustomer_success() throws Exception {
        CustomerResponse response = new CustomerResponse("cus_123", "customer",
                new UserAddress("CO", "Bogota", "Calle 1"), 0L, Currencies.COP, 1000L,
                "john@test.com", new HashMap<>());
        when(customerService.addNewCustomer(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"cus_123","name":"John","email":"john@test.com","address":{"country":"CO","city":"Bogota","line1":"Calle 1"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer created correctly"))
                .andExpect(jsonPath("$.customer.id").value("cus_123"));
    }

    @Test
    @DisplayName("POST /customer - service error")
    void addCustomer_error() throws Exception {
        when(customerService.addNewCustomer(any(CustomerRequest.class)))
                .thenThrow(new RuntimeException("Stripe error"));

        mockMvc.perform(post("/api/customers/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"cus_123","name":"John","email":"john@test.com","address":{"country":"CO","city":"Bogota","line1":"Calle 1"}}
                                """))
                .andExpect(status().isInternalServerError());
    }
}
