package com.parking.core.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity representing a parking session register.
 * <p>
 * Tracks when a vehicle enters and exits the parking lot, along with
 * the total parked time in minutes. Each register is linked to a single {@link Vehicle}.
 * Dates are serialized using the format {@code dd-MM-YYYY HH:mm:ss}.
 * </p>
 *
 * @see Vehicle
 */
@Entity
@Table(name = "register", indexes = @Index(name = "idx_vehicle_exitdate", columnList = "vehicle_id, exitdate"))
public class Register {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    Vehicle vehicle;

    @JsonFormat(pattern = "dd-MM-YYYY HH:mm:ss")
    LocalDateTime entrydate;

    @JsonFormat(pattern = "dd-MM-YYYY HH:mm:ss")
    LocalDateTime exitdate;

    int minutes;

    @Column(name = "phone_number")
    String phoneNumber;

    public Register(){}

    public Register(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getEntrydate() {
        return entrydate;
    }

    public void setEntrydate(LocalDateTime entrydate) {
        this.entrydate = entrydate;
    }

    public LocalDateTime getExitdate() {
        return exitdate;
    }

    public void setExitdate(LocalDateTime exitdate) {
        this.exitdate = exitdate;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
