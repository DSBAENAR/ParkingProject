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

@Service
public class ParkingService {

    private static final Logger log = LoggerFactory.getLogger(ParkingService.class);

    private final VehicleRepository vehicleRepository;
    private final RegisterRepository registerRepository;

    public ParkingService(VehicleRepository vehicleRepository, RegisterRepository registerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.registerRepository = registerRepository;
    }

    public Vehicle getVehicle(Vehicle vehicle) {
        return vehicleRepository.findById(vehicle.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicle " + vehicle.getId() + " not found"));
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        if (vehicles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No vehicles registered");
        }
        log.info("Retrieved {} vehicles", vehicles.size());
        return vehicles;
    }

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

    public Vehicle saveVehicle(Vehicle vehicle) {
        if (vehicleRepository.findById(vehicle.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle already exists");
        }
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Saved new vehicle: {} (type={})", saved.getId(), saved.getType());
        return saved;
    }

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

    public Vehicle updateVehicle(String id, Vehicle toUpdateVehicle) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle does not exist"));

        existing.setType(toUpdateVehicle.getType());
        Vehicle updated = vehicleRepository.save(existing);
        log.info("Updated vehicle {}: type={}", id, updated.getType());
        return updated;
    }
}
