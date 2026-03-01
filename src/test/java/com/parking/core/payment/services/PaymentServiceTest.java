package com.parking.core.payment.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.PaymentStatus;
import com.parking.core.model.Payment;
import com.parking.core.payment.Requests.PaymentRequest;
import com.parking.core.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;
    private MockedStatic<PaymentIntent> paymentIntentStatic;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);
        paymentIntentStatic = Mockito.mockStatic(PaymentIntent.class);
    }

    @AfterEach
    void tearDown() {
        paymentIntentStatic.close();
    }

    @Test
    @DisplayName("createPaymentIntent - basic request")
    void createPaymentIntent_basic() throws Exception {
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_123");
        when(intent.getClientSecret()).thenReturn("secret_123");
        paymentIntentStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenReturn(intent);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequest request = new PaymentRequest(5000L, "usd", null, "ABC-123", null);
        Map<String, String> result = paymentService.createPaymentIntent(request);

        assertEquals("secret_123", result.get("clientSecret"));
        assertEquals("pi_123", result.get("paymentIntentId"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("createPaymentIntent - with customerId")
    void createPaymentIntent_withCustomerId() throws Exception {
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_456");
        when(intent.getClientSecret()).thenReturn("secret_456");
        paymentIntentStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenReturn(intent);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequest request = new PaymentRequest(10000L, "cop", "cus_abc", "DEF-456", null);
        Map<String, String> result = paymentService.createPaymentIntent(request);

        assertNotNull(result.get("clientSecret"));
        assertNotNull(result.get("paymentIntentId"));
    }

    @Test
    @DisplayName("createPaymentIntent - with description")
    void createPaymentIntent_withDescription() throws Exception {
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_789");
        when(intent.getClientSecret()).thenReturn("secret_789");
        paymentIntentStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenReturn(intent);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequest request = new PaymentRequest(2000L, "usd", null, "GHI-789", "Parking fee");
        Map<String, String> result = paymentService.createPaymentIntent(request);

        assertEquals("pi_789", result.get("paymentIntentId"));
    }

    @Test
    @DisplayName("createPaymentIntent - with customerId and description")
    void createPaymentIntent_withAll() throws Exception {
        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_all");
        when(intent.getClientSecret()).thenReturn("secret_all");
        paymentIntentStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenReturn(intent);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequest request = new PaymentRequest(3000L, "usd", "cus_xyz", "JKL-012", "Test description");
        Map<String, String> result = paymentService.createPaymentIntent(request);

        assertEquals("secret_all", result.get("clientSecret"));
    }

    @Test
    @DisplayName("updatePaymentStatus - found")
    void updatePaymentStatus_found() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CREATED);
        when(paymentRepository.findByStripePaymentIntentId("pi_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.updatePaymentStatus("pi_123", PaymentStatus.SUCCEEDED);

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("updatePaymentStatus - not found")
    void updatePaymentStatus_notFound() {
        when(paymentRepository.findByStripePaymentIntentId("pi_missing")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> paymentService.updatePaymentStatus("pi_missing", PaymentStatus.FAILED));
    }
}
