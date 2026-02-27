package com.parking.core.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.repository.VehicleRepository;

/**
 * Service layer for vehicle management and parking payment calculations.
 * <p>
 * Manages vehicle CRUD operations and implements the pricing logic:
 * <ul>
 *   <li><strong>RESIDENT</strong>: 0.05 per minute</li>
 *   <li><strong>NON_RESIDENT</strong>: 0.50 per minute</li>
 *   <li><strong>OFICIAL</strong>: free (0.00)</li>
 * </ul>
 * Also handles the monthly reset cycle: clearing official registers and
 * resetting resident minute counters.
 * </p>
 *
 * @see Vehicle
 * @see VehicleType
 */
@Service
public class ParkingService {

    private static final Logger log = LoggerFactory.getLogger(ParkingService.class);

    private final VehicleRepository vehicleRepository;
    private final RegisterRepository registerRepository;

    public ParkingService(VehicleRepository vehicleRepository, RegisterRepository registerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.registerRepository = registerRepository;
    }

    /**
     * Retrieves a vehicle by its ID.
     *
     * @param vehicle a vehicle object containing the ID to search for
     * @return the found {@link Vehicle}
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if the vehicle does not exist
     */
    public Vehicle getVehicle(Vehicle vehicle) {
        return vehicleRepository.findById(vehicle.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicle " + vehicle.getId() + " not found"));
    }

    /**
     * Retrieves all registered vehicles.
     *
     * @return a list of all {@link Vehicle} entities
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if no vehicles are registered
     */
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        if (vehicles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No vehicles registered");
        }
        log.info("Retrieved {} vehicles", vehicles.size());
        return vehicles;
    }

    /**
     * Calculates the total parking fee for a vehicle based on its type and accumulated minutes.
     * <p>
     * Pricing rules:
     * <ul>
     *   <li>{@code RESIDENT}: totalMinutes × 0.05</li>
     *   <li>{@code OFICIAL}: 0.00 (free)</li>
     *   <li>{@code NON_RESIDENT}: totalMinutes × 0.50</li>
     * </ul>
     * The result is rounded to 2 decimal places using {@code HALF_UP} rounding.
     * </p>
     *
     * @param vehicle the vehicle to calculate payment for
     * @return the total fee rounded to 2 decimal places
     * @throws ResponseStatusException with {@code 400 BAD_REQUEST} if the vehicle has no registers
     */
    public double calculatePayment(Vehicle vehicle) {
        List<Register> registers = registerRepository.findAllByVehicle(vehicle);
        if (registers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No registers found for the vehicle");
        }

        int totalMinutes = registers.stream().mapToInt(Register::getMinutes).sum();

        double price = switch (vehicle.getType()) {
            case RESIDENT -> totalMinutes * 0.05;
            case OFICIAL -> 0;
            default -> totalMinutes * 0.5;
        };

        double result = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).doubleValue();
        log.info("Calculated payment for vehicle {}: {} ({}min, type={})",
                vehicle.getId(), result, totalMinutes, vehicle.getType());
        return result;
    }

    /**
     * Saves a new vehicle in the system.
     *
     * @param vehicle the vehicle to save
     * @return the saved {@link Vehicle}
     * @throws ResponseStatusException with {@code 400 BAD_REQUEST} if a vehicle with the same ID already exists
     */
    public Vehicle saveVehicle(Vehicle vehicle) {
        if (vehicleRepository.findById(vehicle.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle already exists");
        }
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Saved new vehicle: {} (type={})", saved.getId(), saved.getType());
        return saved;
    }

    /**
     * Performs the monthly reset cycle.
     * <p>
     * This operation:
     * <ol>
     *   <li>Resets all resident register minutes to zero</li>
     *   <li>Deletes all official vehicle registers</li>
     * </ol>
     * </p>
     *
     * @return a map containing a success message and the count of deleted official registers
     */
    public Map<String, Object> monthStarts() {
        List<Register> oficials = registerRepository.findAllByVehicle_Type(VehicleType.OFICIAL);
        List<Register> residents = registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT);

        for (Register register : residents) {
            register.setMinutes(0);
        }
        registerRepository.saveAll(residents);

        int count = oficials.size();
        registerRepository.deleteAll(oficials);

        log.info("Month reset: deleted {} official registers, reset {} resident registers", count, residents.size());
        return Map.of(
                "message", "Deleted all official registers",
                "deletedCount", count);
    }

    /**
     * Updates the type of an existing vehicle.
     *
     * @param id              the ID of the vehicle to update
     * @param toUpdateVehicle a vehicle object containing the new type
     * @return the updated {@link Vehicle}
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if the vehicle does not exist
     */
    public Vehicle updateVehicle(String id, Vehicle toUpdateVehicle) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle does not exist"));

        existing.setType(toUpdateVehicle.getType());
        Vehicle updated = vehicleRepository.save(existing);
        log.info("Updated vehicle {}: type={}", id, updated.getType());
        return updated;
    }
}
