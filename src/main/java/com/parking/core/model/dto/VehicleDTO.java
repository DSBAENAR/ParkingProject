package com.parking.core.model.dto;

import com.parking.core.enums.VehicleType;

/**
 * Data Transfer Object for {@link com.parking.core.model.Vehicle}.
 *
 * @param id   the license plate (vehicle identifier)
 * @param type the vehicle classification ({@link VehicleType})
 */
public record VehicleDTO(
    String id,
    VehicleType type
) {}
