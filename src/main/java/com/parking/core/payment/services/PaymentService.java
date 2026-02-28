package com.parking.core.payment.services;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.PaymentStatus;
import com.parking.core.model.Payment;
import com.parking.core.payment.Requests.PaymentRequest;
import com.parking.core.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Map<String, String> createPaymentIntent(PaymentRequest request) throws StripeException {
        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(request.amount())
                .setCurrency(request.currency().toLowerCase())
                .addPaymentMethodType("card");

        if (request.customerId() != null && !request.customerId().isBlank()) {
            paramsBuilder.setCustomer(request.customerId());
        }
        if (request.description() != null && !request.description().isBlank()) {
            paramsBuilder.setDescription(request.description());
        }

        PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());

        Payment payment = new Payment();
        payment.setStripePaymentIntentId(intent.getId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency().toUpperCase());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setVehicleId(request.vehicleId());
        payment.setCustomerId(request.customerId());
        payment.setDescription(request.description());
        paymentRepository.save(payment);

        return Map.of(
                "clientSecret", intent.getClientSecret(),
                "paymentIntentId", intent.getId());
    }

    public void updatePaymentStatus(String stripePaymentIntentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment not found for intent: " + stripePaymentIntentId));
        payment.setStatus(newStatus);
        paymentRepository.save(payment);
    }
}
