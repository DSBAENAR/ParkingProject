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

/**
 * Service layer for managing parking register operations.
 * <p>
 * Handles vehicle entry and exit from the parking lot, tracking timestamps
 * and calculating the duration of each parking session in minutes.
 * </p>
 *
 * @see Register
 * @see Vehicle
 */
@Service
public class RegisterService {

    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);

    private final RegisterRepository registerRepository;
    private final VehicleRepository vehicleRepository;

    public RegisterService(RegisterRepository registerRepository, VehicleRepository vehicleRepository) {
        this.registerRepository = registerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Retrieves all parking registers.
     *
     * @return a list of all {@link Register} entries
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if no registers exist
     */
    public List<Register> getAllRegisters() {
        List<Register> registers = registerRepository.findAll();
        if (registers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No registers found");
        }
        log.info("Retrieved {} registers", registers.size());
        return registers;
    }

    /**
     * Registers a vehicle's entrance into the parking lot.
     * <p>
     * Verifies the vehicle exists in the system and does not already have an active
     * (non-exited) register. Sets the entry timestamp to the current time.
     * </p>
     *
     * @param vehicleToRegister the vehicle entering the parking lot
     * @return the created {@link Register} with entry date set
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if the vehicle is not registered
     * @throws ResponseStatusException with {@code 400 BAD_REQUEST} if the vehicle already has an active register
     */
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

    /**
     * Processes a vehicle's departure from the parking lot.
     * <p>
     * Finds the active register (no exit date) for the vehicle, sets the exit timestamp,
     * and calculates the total parked time in minutes.
     * </p>
     *
     * @param vehicle the vehicle leaving the parking lot
     * @return the updated {@link Register} with exit date and minutes calculated
     * @throws ResponseStatusException with {@code 400 BAD_REQUEST} if no active register exists for the vehicle
     */
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
