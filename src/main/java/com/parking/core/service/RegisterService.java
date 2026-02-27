package com.parking.core.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.repository.VehicleRepository;

@Service
public class RegisterService {

    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);

    private final RegisterRepository registerRepository;
    private final VehicleRepository vehicleRepository;

    public RegisterService(RegisterRepository registerRepository, VehicleRepository vehicleRepository) {
        this.registerRepository = registerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public List<Register> getAllRegisters() {
        List<Register> registers = registerRepository.findAll();
        if (registers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No registers found");
        }
        log.info("Retrieved {} registers", registers.size());
        return registers;
    }

    public Register registerVehicleEntrance(Vehicle vehicleToRegister) {
        Vehicle vehicle = vehicleRepository.findById(vehicleToRegister.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicle " + vehicleToRegister.getId() + " not found"));

        boolean existingRegister = registerRepository.existsByVehicleAndExitdateIsNull(vehicle);
        if (existingRegister) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Register already exists for this vehicle");
        }

        Register register = new Register(vehicle);
        register.setEntrydate(LocalDateTime.now());

        Register saved = registerRepository.save(register);
        log.info("Vehicle {} entered parking - Register #{}", vehicle.getId(), saved.getId());
        return saved;
    }

    public Register leaveVehicle(Vehicle vehicle) {
        Register existing = registerRepository.findByVehicleAndExitdateIsNull(vehicle)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active register found for vehicle"));

        existing.setExitdate(LocalDateTime.now());
        int minutes = (int) ChronoUnit.MINUTES.between(existing.getEntrydate(), existing.getExitdate());
        existing.setMinutes(minutes);

        Register saved = registerRepository.save(existing);
        log.info("Vehicle {} left parking after {} minutes - Register #{}", vehicle.getId(), minutes, saved.getId());
        return saved;
    }
}
