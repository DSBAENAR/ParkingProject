package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.model.Vehicle;
import com.parking.core.service.ParkingService;

import java.util.Collections;
import java.util.LinkedHashMap;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("api/v1/parking/vehicles")
public class ParkingHandler {
    private final ParkingService parkingService;

    public ParkingHandler(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    /**
     * Handles HTTP GET requests to the root endpoint ("/").
     * Retrieves a list of all vehicles from the parking service.
     *
     * @return ResponseEntity containing a map with the key "vehicles" and the list of vehicles as the value,
     * or an error message with HTTP 500 status if an exception occurs.
     */
    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok().body(Collections.singletonMap("vehicles", parkingService.getAllVehicles()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    /**
     * Handles HTTP POST requests to save a new vehicle.
     * <p>
     * Expects a {@link Vehicle} object in the request body and attempts to persist it using the parking service.
     * On success, returns a response containing a success message and the saved vehicle.
     * On failure, returns an internal server error with the exception message.
     *
     * @param vehicle the {@link Vehicle} object to be saved, provided in the request body
     * @return a {@link ResponseEntity} containing a success message and the saved vehicle,
     * or an error message in case of failure
     */

    @PostMapping("/")
    public ResponseEntity<?> saveVehicle(@RequestBody Vehicle vehicle) {
        try {
            Vehicle responseVehicle = parkingService.saveVehicle(vehicle);

            Map<String,Object> response = new LinkedHashMap<>();
            response.put("message", "Vehicle created successfully");
            response.put("vehicle", responseVehicle);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
        }
    }

    

    /**
     * Handles POST requests to calculate the payment for a parked vehicle.
     * <p>
     * Expects a {@link Vehicle} object in the request body and returns a response containing
     * the calculated parking price and the vehicle details.
     * </p>
     *
     * @param vehicle the {@link Vehicle} object containing parking details
     * @return a {@link ResponseEntity} with a map containing the calculated price and vehicle information,
     * or an error message if the calculation fails
     */
    @PostMapping("/pay")
    public ResponseEntity<?> calculatePayment(@RequestBody Vehicle vehicle) {
        try {
            double vehicleParkingPrice = parkingService.calculatePayment(vehicle);
            Map<String,Object> response = new LinkedHashMap<>();
            response.put("price", vehicleParkingPrice);
            response.put("Vehicle", vehicle);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve the starting dates of the current month for parking records.
     *
     * @return a {@link ResponseEntity} containing the result of {@code parkingService.monthStarts()}.
     */
    @GetMapping("/startsMonth")
    public ResponseEntity<?> getMethodName() {
        return ResponseEntity
                    .ok()
                    .body(parkingService.monthStarts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable String id, @RequestBody Vehicle vehicle) {
        try {
            Vehicle updated = parkingService.updateVehicle(id,vehicle);
            Map<String,Object> response = new LinkedHashMap<>();
            response.put("message", "Vehicle updated successfully");
            response.put("vehicle", updated);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
        }
    }
    
}
