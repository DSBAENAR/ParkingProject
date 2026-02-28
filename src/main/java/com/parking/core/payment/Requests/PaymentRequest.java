package com.parking.core.payment.Requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
    @Min(1) long amount,
    @NotBlank String currency,
    String customerId,
    String vehicleId,
    String description
) {}
