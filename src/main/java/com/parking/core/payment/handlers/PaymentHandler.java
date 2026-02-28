package com.parking.core.payment.handlers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.payment.Requests.PaymentRequest;
import com.parking.core.payment.services.PaymentService;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/payments")
public class PaymentHandler {

    private final PaymentService paymentService;

    public PaymentHandler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @Valid @RequestBody PaymentRequest request) throws StripeException {
        return ResponseEntity.ok(paymentService.createPaymentIntent(request));
    }
}
