package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.model.Vehicle;
import com.parking.core.service.ParkingService;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/parking/vehicles")
public class ParkingHandler {
    private final ParkingService parkingService;

    public ParkingHandler(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAll() {
        return ResponseEntity.ok(Map.of("vehicles", parkingService.getAllVehicles()));
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> saveVehicle(@Valid @RequestBody Vehicle vehicle) {
        Vehicle saved = parkingService.saveVehicle(vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Vehicle created successfully");
        response.put("vehicle", saved);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> calculatePayment(@Valid @RequestBody Vehicle vehicle) {
        double price = parkingService.calculatePayment(vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("price", price);
        response.put("vehicle", vehicle);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/startsMonth")
    public ResponseEntity<Map<String, Object>> startsMonth() {
        return ResponseEntity.ok(parkingService.monthStarts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVehicle(@PathVariable String id, @Valid @RequestBody Vehicle vehicle) {
        Vehicle updated = parkingService.updateVehicle(id, vehicle);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Vehicle updated successfully");
        response.put("vehicle", updated);
        return ResponseEntity.ok(response);
    }
}
