package com.parking.core.payment.handlers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.payment.Requests.PaymentRequest;
import com.parking.core.payment.Requests.SendPaymentLinkRequest;
import com.parking.core.payment.services.PaymentService;
import com.parking.core.service.RegisterService;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/payments")
public class PaymentHandler {

    private final PaymentService paymentService;
    private final RegisterService registerService;

    public PaymentHandler(PaymentService paymentService, RegisterService registerService) {
        this.paymentService = paymentService;
        this.registerService = registerService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @Valid @RequestBody PaymentRequest request) throws StripeException {
        return ResponseEntity.ok(paymentService.createPaymentIntent(request));
    }

    @PostMapping("/send-link")
    public ResponseEntity<Map<String, String>> sendPaymentLink(
            @Valid @RequestBody SendPaymentLinkRequest request) {
        registerService.leaveVehicleAndSendLink(request);
        return ResponseEntity.ok(Map.of("message", "Link de pago enviado"));
    }

    @PostMapping("/cash")
    public ResponseEntity<Map<String, String>> payCash(
            @RequestBody Map<String, String> body) {
        registerService.leaveVehicleCash(body.get("vehicleId"));
        return ResponseEntity.ok(Map.of("message", "Pago en efectivo registrado"));
    }
}
