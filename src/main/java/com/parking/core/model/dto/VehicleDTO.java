package com.parking.core.model.dto;

import com.parking.core.enums.VehicleType;

public record VehicleDTO(
    String id,
    VehicleType type
) {}
