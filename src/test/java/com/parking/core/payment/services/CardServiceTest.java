package com.parking.core.payment.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.Requests.UserAddress;
import com.parking.core.payment.response.CardResponse;
import com.parking.core.payment.utils.CardRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodCreateParams;

class CardServiceTest {

    private CardService cardService;
    private MockedStatic<PaymentMethod> paymentMethodStatic;

    @BeforeEach
    void setUp() {
        cardService = new CardService();
        paymentMethodStatic = Mockito.mockStatic(PaymentMethod.class);
    }

    @AfterEach
    void tearDown() {
        paymentMethodStatic.close();
    }

    private CardRequest buildRequest() {
        var customer = new CustomerRequest("cus_123", "John", "john@test.com",
                new UserAddress("CO", "Bogota", "Calle 1"));
        return new CardRequest(customer, "123", 12L, 2030L, "4242424242424242", Currencies.COP);
    }

    @Test
    @DisplayName("attachCardToCustomer - success")
    void attachCardToCustomer_success() throws StripeException {
        PaymentMethod pm = mock(PaymentMethod.class);
        PaymentMethod.Card card = mock(PaymentMethod.Card.class);

        when(card.getBrand()).thenReturn("visa");
        when(card.getCountry()).thenReturn("CO");
        when(card.getExpMonth()).thenReturn(12L);
        when(card.getExpYear()).thenReturn(2030L);
        when(card.getFunding()).thenReturn("credit");
        when(card.getLast4()).thenReturn("4242");
        when(card.getChecks()).thenReturn(null);
        when(pm.getCard()).thenReturn(card);
        when(pm.getType()).thenReturn("card");

        paymentMethodStatic.when(() -> PaymentMethod.create(any(PaymentMethodCreateParams.class)))
                .thenReturn(pm);

        CardResponse response = cardService.attachCardToCustomer(buildRequest());

        assertNotNull(response);
        assertNotNull(response.card());
        assertEquals("visa", response.card().brand());
        assertEquals("4242", response.card().last4());
    }

    @Test
    @DisplayName("attachCardToCustomer - Stripe create fails")
    void attachCardToCustomer_stripeCreateFails() {
        paymentMethodStatic.when(() -> PaymentMethod.create(any(PaymentMethodCreateParams.class)))
                .thenThrow(new RuntimeException("Stripe error"));

        assertThrows(RuntimeException.class, () -> cardService.attachCardToCustomer(buildRequest()));
    }

    @Test
    @DisplayName("attachCardToCustomer - attach fails")
    void attachCardToCustomer_attachFails() throws StripeException {
        PaymentMethod pm = mock(PaymentMethod.class);

        paymentMethodStatic.when(() -> PaymentMethod.create(any(PaymentMethodCreateParams.class)))
                .thenReturn(pm);
        doThrow(new RuntimeException("Attach failed")).when(pm).attach(any(PaymentMethodAttachParams.class));

        assertThrows(RuntimeException.class, () -> cardService.attachCardToCustomer(buildRequest()));
    }
}
