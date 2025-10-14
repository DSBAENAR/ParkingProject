package com.parking.core.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final VehicleRepository vehicleRepository;
    private final RegisterRepository registerRepository;
    public ParkingService(VehicleRepository vehicleRepository, RegisterRepository registerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.registerRepository = registerRepository;
    }

    /**
     * The function `getVehicle` retrieves a vehicle from a repository by its ID.
     * 
     * @param vehicle The `getVehicle` method you provided takes a `Vehicle` object as a parameter.
     * This method retrieves a `Vehicle` object from a repository based on the provided `vehicle`
     * object's ID. If the `Vehicle` with the specified ID is not found in the repository, it throws a
     * `
     * @return The method `getVehicle` returns the existing vehicle with the specified ID from the
     * `vehicleRepository`. If the vehicle is not found in the repository, it throws a
     * `RuntimeException` with a message indicating that the vehicle was not found.
     */
    public Vehicle getVehicle(Vehicle vehicle){
        Optional<Vehicle> existingVehicle = vehicleRepository.findById(vehicle.getId());

        if (!existingVehicle.isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Vehicle " + vehicle.getId() + " not found");

        return existingVehicle.get();
    }

    /**
     * The function `getAllVehicles` retrieves all vehicles from a repository and throws an exception
     * if no vehicles are found.
     * 
     * @return A list of all vehicles stored in the vehicle repository is being returned. If the list
     * is empty, a RuntimeException with the message "There are not vehicles registered" will be
     * thrown.
     *
     */
    public List<Vehicle> getAllVehicles(){
        List<Vehicle> vehicles = vehicleRepository.findAll();

        if(vehicles.isEmpty()) throw new RuntimeException("There are not vehicles resgistered");

        return vehicles;
    }

    
    /**
     * The function calculates the payment for a vehicle based on the total minutes parked and the
     * vehicle type.
     * 
     * @param vehicle Vehicle object that contains information about a vehicle, such as its type
     * (RESIDENT or other) and other details.
     * @return The method `calculatePayment` returns a `double` value, which represents the calculated
     * payment amount for a given `Vehicle` based on the total minutes and the vehicle type.
     */
    public double calculatePayment(Vehicle vehicle){
        List<Register> registers = registerRepository.findAllByVehicle(vehicle);

        if (registers.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are not registers for the vehicle");
        int totalMinutes = 0;
        for (Register register : registers) {
            totalMinutes += register.getMinutes();
        }

        double price = 0;
        switch (vehicle.getType()) {
            case RESIDENT:
                price = totalMinutes * 0.05;
                break;
            
            case OFICIAL:
                break;
            default:
            price = totalMinutes * 0.5;
                break;
        }
        
        return BigDecimal.valueOf(price).setScale(2,RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * The function `saveVehicle` checks if a vehicle already exists in the repository before saving
     * it.
     * 
     * @param vehicle The `saveVehicle` method takes a `Vehicle` object as a parameter. This object
     * represents the vehicle that needs to be saved in the database. The method first checks if a
     * vehicle with the same ID already exists in the database. If it does, it throws a
     * `ResponseStatusException`
     * @return The method is returning the saved `Vehicle` object after checking if it already exists
     * in the repository.
     */
    public Vehicle saveVehicle(Vehicle vehicle){
        Optional<Vehicle> existing = vehicleRepository.findById(vehicle.getId());

        if (existing.isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Could not save vehicle, already exists");
        return vehicleRepository.save(vehicle);
    }

    


    /**
     * Deletes all register entries of vehicles with type OFICIAL from the repository at the start of the month.
     * 
     * @return a map containing a message and the count of deleted official registers.
     */
    public Map<String, Object> monthStarts() {
        List<Register> oficials = registerRepository.findAllByVehicle_Type(VehicleType.OFICIAL);
        List<Register> residents = registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT);

        for (Register register : residents) {
            register.setMinutes(0);
        }

        int count = oficials.size();
        registerRepository.deleteAll(oficials);

        return Map.of(
            "message", "Deleted all official registers",
            "deletedCount", count
        );
    }

    /**
     * Updates the type of an existing vehicle identified by its ID.
     *
     * @param id the unique identifier of the vehicle to update
     * @param toUpdateVehicle the vehicle object containing updated information
     * @return the updated type of Vehicle object after saving to the repository
     * @throws ResponseStatusException if the vehicle does not exist (BAD_GATEWAY or BAD_REQUEST)
     */
    public Vehicle updateVehicle(String id,Vehicle toUpdateVehicle){

        Vehicle existing = vehicleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Vehicle does not exist"));

        if (existing == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Vehicle does not exist");

        existing.setType(toUpdateVehicle.getType());

        return vehicleRepository.save(existing);
    }

}
