package com.parking.core.model;

import com.parking.core.enums.VehicleType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class Vehicle {
    @Id
    String id;

    @Enumerated(EnumType.STRING)
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
