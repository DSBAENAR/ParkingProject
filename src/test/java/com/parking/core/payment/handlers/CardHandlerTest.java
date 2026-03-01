package com.parking.core.payment.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.payment.response.CardResponse;
import com.parking.core.payment.services.CardService;
import com.parking.core.payment.utils.CardInfo;
import com.parking.core.payment.utils.CardRequest;

@WebMvcTest(CardHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CardHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /card - success")
    void addCard_success() throws Exception {
        CardInfo info = new CardInfo("visa", "card", "CO", 12, 2030, "credit", "4242", null);
        CardResponse response = new CardResponse("cus_123", info);
        when(cardService.attachCardToCustomer(any(CardRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cards/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customer": {"id":"cus_123","name":"John","email":"john@test.com","address":{"country":"CO","city":"Bogota","line1":"Calle 1"}},
                                    "cvc": "123",
                                    "expMonth": 12,
                                    "expYear": 2030,
                                    "number": "4242424242424242",
                                    "currency": "COP"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.brand").value("visa"));
    }

    @Test
    @DisplayName("POST /card - service throws exception")
    void addCard_serviceError() throws Exception {
        when(cardService.attachCardToCustomer(any(CardRequest.class)))
                .thenThrow(new RuntimeException("Stripe error"));

        mockMvc.perform(post("/api/cards/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customer": {"id":"cus_123","name":"John","email":"john@test.com","address":{"country":"CO","city":"Bogota","line1":"Calle 1"}},
                                    "cvc": "123",
                                    "expMonth": 12,
                                    "expYear": 2030,
                                    "number": "4242424242424242",
                                    "currency": "COP"
                                }
                                """))
                .andExpect(status().isInternalServerError());
    }
}
