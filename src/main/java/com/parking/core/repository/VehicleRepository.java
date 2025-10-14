package com.parking.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.core.model.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

}
