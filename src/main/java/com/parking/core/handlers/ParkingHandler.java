package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.model.Vehicle;
import com.parking.core.service.ParkingService;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;

/**
 * REST controller for vehicle management and payment endpoints.
 * <p>
 * Base path: {@code /api/v1/parking/vehicles}
 * </p>
 *
 * @see ParkingService
 */
@RestController
@RequestMapping("api/v1/parking/vehicles")
public class ParkingHandler {
    private final ParkingService parkingService;

    public ParkingHandler(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    /**
     * Retrieves all registered vehicles.
     *
     * @return {@code 200 OK} with the list of vehicles
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAll() {
        return ResponseEntity.ok(Map.of("vehicles", parkingService.getAllVehicles()));
    }

    /**
     * Registers a new vehicle in the parking system.
     *
     * @param vehicle the vehicle data (validated)
     * @return {@code 200 OK} with a success message and the saved vehicle
     */
    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> saveVehicle(@Valid @RequestBody Vehicle vehicle) {
        Vehicle saved = parkingService.saveVehicle(vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Vehicle created successfully");
        response.put("vehicle", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Calculates the parking fee for a vehicle.
     *
     * @param vehicle the vehicle to calculate payment for (validated)
     * @return {@code 200 OK} with the calculated price and vehicle info
     */
    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> calculatePayment(@Valid @RequestBody Vehicle vehicle) {
        double price = parkingService.calculatePayment(vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("price", price);
        response.put("vehicle", vehicle);
        return ResponseEntity.ok(response);
    }

    /**
     * Triggers the monthly reset: deletes official registers and resets resident minutes.
     *
     * @return {@code 200 OK} with a summary of the reset operation
     */
    @PostMapping("/startsMonth")
    public ResponseEntity<Map<String, Object>> startsMonth() {
        return ResponseEntity.ok(parkingService.monthStarts());
    }

    /**
     * Updates the type of an existing vehicle.
     *
     * @param id      the plate/ID of the vehicle to update
     * @param vehicle the vehicle object with the new type (validated)
     * @return {@code 200 OK} with a success message and the updated vehicle
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVehicle(@PathVariable String id, @Valid @RequestBody Vehicle vehicle) {
        Vehicle updated = parkingService.updateVehicle(id, vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Vehicle updated successfully");
        response.put("vehicle", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVehicle(@PathVariable String id) {
        parkingService.deleteVehicle(id);
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully"));
    }
}
