package com.parking.core.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
@Repository
public interface RegisterRepository extends JpaRepository<Register,Long>{
    boolean existsByVehicleAndExitdateIsNull(Vehicle vehicle);

    Optional<Register> findByVehicle(Vehicle vehicle);

    Optional<Register> findByVehicleAndExitdateIsNull(Vehicle vehicle);

    List<Register> findAllByVehicle_Type(VehicleType type);

    List<Register> findAllByVehicle(Vehicle vehicle);
}
