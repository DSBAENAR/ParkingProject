package com.parking.core.payment.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.enums.PaymentStatus;
import com.parking.core.payment.services.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

@WebMvcTest(WebhookHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /stripe - payment_intent.succeeded")
    void webhook_succeeded() throws Exception {
        try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
            Event event = mock(Event.class);
            when(event.getType()).thenReturn("payment_intent.succeeded");

            EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_123");
            when(deserializer.getObject()).thenReturn(Optional.of(intent));
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);

            webhookStatic.when(() -> Webhook.constructEvent(any(), any(), any()))
                    .thenReturn(event);

            mockMvc.perform(post("/api/webhooks/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", "sig_test")
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("received"));

            verify(paymentService).updatePaymentStatus("pi_123", PaymentStatus.SUCCEEDED);
        }
    }

    @Test
    @DisplayName("POST /stripe - payment_intent.payment_failed")
    void webhook_failed() throws Exception {
        try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
            Event event = mock(Event.class);
            when(event.getType()).thenReturn("payment_intent.payment_failed");

            EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_456");
            when(deserializer.getObject()).thenReturn(Optional.of(intent));
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);

            webhookStatic.when(() -> Webhook.constructEvent(any(), any(), any()))
                    .thenReturn(event);

            mockMvc.perform(post("/api/webhooks/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", "sig_test")
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("received"));

            verify(paymentService).updatePaymentStatus("pi_456", PaymentStatus.FAILED);
        }
    }

    @Test
    @DisplayName("POST /stripe - invalid signature")
    void webhook_invalidSignature() throws Exception {
        try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
            webhookStatic.when(() -> Webhook.constructEvent(any(), any(), any()))
                    .thenThrow(new SignatureVerificationException("Invalid sig", "sig_header"));

            mockMvc.perform(post("/api/webhooks/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", "invalid_sig")
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid signature"));
        }
    }

    @Test
    @DisplayName("POST /stripe - unknown event type")
    void webhook_unknownEvent() throws Exception {
        try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
            Event event = mock(Event.class);
            when(event.getType()).thenReturn("charge.refunded");

            webhookStatic.when(() -> Webhook.constructEvent(any(), any(), any()))
                    .thenReturn(event);

            mockMvc.perform(post("/api/webhooks/stripe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", "sig_test")
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("received"));

            verify(paymentService, never()).updatePaymentStatus(any(), any());
        }
    }
}
