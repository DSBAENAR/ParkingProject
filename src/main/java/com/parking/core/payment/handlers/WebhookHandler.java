package com.parking.core.payment.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.enums.PaymentStatus;
import com.parking.core.payment.services.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

@RestController
@RequestMapping("api/webhooks")
public class WebhookHandler {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public WebhookHandler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        String type = event.getType();

        if ("payment_intent.succeeded".equals(type) || "payment_intent.payment_failed".equals(type)) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);

            if (intent != null) {
                PaymentStatus status = "payment_intent.succeeded".equals(type)
                        ? PaymentStatus.SUCCEEDED
                        : PaymentStatus.FAILED;
                paymentService.updatePaymentStatus(intent.getId(), status);
            }
        }

        return ResponseEntity.ok("received");
    }
}
