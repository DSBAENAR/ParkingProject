package com.parking.core.payment.Requests;

import com.parking.core.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SendPaymentLinkRequest(
        @NotBlank String vehicleId,
        @NotNull VehicleType vehicleType,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$") String phoneNumber,
        @NotBlank String channel
) {}
