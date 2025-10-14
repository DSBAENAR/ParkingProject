package com.parking.core.service;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.repository.VehicleRepository;

@Service
public class RegisterService {
    private final RegisterRepository registerRepository;
    private final VehicleRepository vehicleRepository;

    public RegisterService(RegisterRepository registerRepository,VehicleRepository vehicleRepository) {
        this.registerRepository = registerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Retrieves all register entries from the repository.
     *
     * @return a list of all {@link Register} objects.
     * @throws ResponseStatusException if no registers are found, with HTTP status 500 (Internal Server Error).
     */
    public List<Register> getAllRegisters(){
        List<Register> registers = registerRepository.findAll();

        if (registers.isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "There are not registers");

        return registers;
    }

    /**
     * The function `registerVehicleEntrance` registers a vehicle's entrance by creating a new entry in
     * the register repository if the vehicle is not already registered.
     * 
     * @param vehicleToRegister The `registerVehicleEntrance` method takes a `Vehicle` object as a
     * parameter named `vehicleToRegister`. This method registers the entrance of a vehicle by creating
     * a new `Register` object, setting the entry date to the current moment, and saving it to the
     * repository.
     * @return The method `registerVehicleEntrance` is returning a `Register` object after saving it in
     * the `registerRepository`.
     */
    public Register registerVehicleEntrance(Vehicle vehicleToRegister){
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleToRegister.getId());

        LocalDateTime moment = LocalDateTime.now();

        Register register = new Register(vehicle.get());

        register.setEntrydate(moment);

        boolean existingRegister = registerRepository.existsByVehicleAndExitdateIsNull(vehicle.get());

        if (existingRegister) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Register already exits");
        
        return registerRepository.save(register);
    }

    
    /**
     * The `leaveVehicle` function updates the exit date of a vehicle in the register and saves the
     * changes.
     * 
     * @param vehicle The `leaveVehicle` method takes a `Vehicle` object as a parameter. This method is
     * used to update the exit date of a vehicle in the register when it leaves a certain location or
     * parking area. The method first checks if there is an existing entry in the register for the
     * provided vehicle.
     * @return The `leaveVehicle` method returns a `Register` object after updating the exit date of
     * the vehicle in the register and saving the changes in the repository.
     */
    public Register leaveVehicle(Vehicle vehicle){
        
        Optional<Register> existing = registerRepository.findByVehicleAndExitdateIsNull(vehicle);

        if (!existing.isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Register not found");

        existing.get().setExitdate(LocalDateTime.now());

        int minutes = (int) ChronoUnit.MINUTES.between(existing.get().getEntrydate(), existing.get().getExitdate());

        existing.get().setMinutes(minutes);

        return registerRepository.save(existing.get());
    }
    
}
