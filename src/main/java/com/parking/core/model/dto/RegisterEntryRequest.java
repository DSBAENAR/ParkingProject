package com.parking.core.model.dto;

import com.parking.core.enums.VehicleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterEntryRequest(
    @NotBlank String vehicleId,
    @NotNull VehicleType vehicleType,
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$") String phoneNumber,
    String notificationChannel
) {}
