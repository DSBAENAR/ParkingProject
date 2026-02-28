package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.service.RegisterService;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

/**
 * REST controller for parking register operations (vehicle entry and exit).
 * <p>
 * Base path: {@code /api/v1/parking}
 * </p>
 *
 * @see RegisterService
 */
@RestController
@RequestMapping("api/v1/parking")
public class RegisterHandler {

    private final RegisterService registerService;

    public RegisterHandler(RegisterService registerService) {
        this.registerService = registerService;
    }

    /**
     * Retrieves all parking registers.
     *
     * @return {@code 200 OK} with the list of all registers
     */
    @GetMapping("/registers")
    public ResponseEntity<Map<String, Object>> getAll() {
        return ResponseEntity.ok(Map.of("registers", registerService.getAllRegisters()));
    }

    /**
     * Registers a vehicle's entrance into the parking system.
     *
     * @param vehicle the vehicle details from the request body
     * @return success message and the created register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerVehicle(@Valid @RequestBody Vehicle vehicle) {
        Register register = registerService.registerVehicleEntrance(vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Register created successfully");
        response.put("register", register);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Processes a vehicle's departure from the parking lot.
     *
     * @param vehicle the vehicle leaving the parking lot
     * @return success message and the updated register
     */
    @PostMapping("/leave")
    public ResponseEntity<Map<String, Object>> leaveVehicle(@Valid @RequestBody Vehicle vehicle) {
        Register register = registerService.leaveVehicle(vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Register updated successfully");
        response.put("register", register);
        return ResponseEntity.ok(response);
    }
}
