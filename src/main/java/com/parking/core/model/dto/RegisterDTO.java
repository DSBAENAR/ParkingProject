package com.parking.core.model.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.parking.core.model.Register}.
 *
 * @param id        the register auto-generated identifier
 * @param vehicle   the associated vehicle DTO
 * @param entryDate the date/time the vehicle entered the parking lot
 * @param exitDate  the date/time the vehicle exited (may be {@code null} if still parked)
 * @param minutes   the total minutes the vehicle has been parked
 */
public record RegisterDTO(
    long id,
    VehicleDTO vehicle,
    LocalDateTime entryDate,
    LocalDateTime exitDate,
    int minutes
) {}
