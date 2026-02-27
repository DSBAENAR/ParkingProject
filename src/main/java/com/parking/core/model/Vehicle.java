package com.parking.core.model;

import com.parking.core.enums.VehicleType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JPA entity representing a vehicle in the parking system.
 * <p>
 * The vehicle ID typically corresponds to a license plate number.
 * The type determines the pricing rules applied during payment calculation.
 * </p>
 *
 * @see VehicleType
 */
@Entity
public class Vehicle {
    @Id
    @NotBlank(message = "Vehicle ID is required")
    String id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Vehicle type is required")
    VehicleType type;

    public Vehicle(String id, VehicleType type) {
        this.id = id;
        this.type = type;
    }

    public Vehicle() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }


    
}
