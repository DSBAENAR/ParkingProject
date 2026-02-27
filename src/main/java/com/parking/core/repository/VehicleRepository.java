package com.parking.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.core.model.Vehicle;

/**
 * Spring Data JPA repository for {@link Vehicle} entities.
 * <p>
 * The primary key is the vehicle's license plate ({@code String}).
 * </p>
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

}
