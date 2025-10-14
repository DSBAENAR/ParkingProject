package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.service.ParkingService;
import com.parking.core.service.RegisterService;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/v1/parking")
public class RegisterHandler {

    private final RegisterService registerService;
    public RegisterHandler(RegisterService registerService, ParkingService parkingService) {
        this.registerService = registerService;
    }

    @GetMapping("/registers")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(Collections.singletonMap("registers", registerService.getAllRegisters()));
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
        }
    }

    /**
     * Handles HTTP POST requests to register a vehicle's entrance into the parking system.
     * 
     * @param vehicle the {@link Vehicle} object containing vehicle details from the request body
     * @return a {@link ResponseEntity} containing a success message and the created {@link Register} object,
     *  or an error message with the appropriate HTTP status if registration fails
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerVehicle(@RequestBody Vehicle vehicle) {
        try {
            Register register = registerService.registerVehicleEntrance(vehicle); 

            Map<String,Object> response = new LinkedHashMap<>();
            response.put("message", "Register created successfully");
            response.put("register", register);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
        }
    }

    /**
     * Handles HTTP POST requests to the "/leave" endpoint for vehicle departure.
     * <p>
     * This method receives a {@link Vehicle} object in the request body and attempts to process
     * the vehicle's departure from the parking lot using the {@code parkingService}. If successful,
     * it returns a response containing a success message and the updated {@link Register} object.
     * If an error occurs (e.g., the vehicle is not found), it returns an appropriate error message
     * and HTTP status code.
     *
     * @param vehicle the {@link Vehicle} object representing the vehicle leaving the parking lot
     * @return a {@link ResponseEntity} containing a success message and the updated register,
     * or an error message with the corresponding HTTP status code
     */
    @PostMapping("/leave")
    public ResponseEntity<?> leaveVehicle(@RequestBody Vehicle vehicle) {
        try {
            Register register = registerService.leaveVehicle(vehicle); 

            Map<String,Object> response = new LinkedHashMap<>();
            response.put("message", "Register updated successfully");
            response.put("register", register);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
        }
    }
    
}
