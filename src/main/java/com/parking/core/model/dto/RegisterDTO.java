package com.parking.core.model.dto;

import java.time.LocalDateTime;

public record RegisterDTO(
    long id,
    VehicleDTO vehicle,
    LocalDateTime entryDate,
    LocalDateTime exitDate,
    int minutes
) {}
