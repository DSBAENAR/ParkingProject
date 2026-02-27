package com.parking.core.model.dto;

import com.parking.core.model.Register;
import com.parking.core.model.User;
import com.parking.core.model.Vehicle;

/**
 * Utility class for converting entities to DTOs.
 * Prevents exposing JPA entities directly in API responses.
 */
public final class DtoMapper {

    private DtoMapper() {}

    public static VehicleDTO toVehicleDTO(Vehicle vehicle) {
        return new VehicleDTO(vehicle.getId(), vehicle.getType());
    }

    public static RegisterDTO toRegisterDTO(Register register) {
        return new RegisterDTO(
            register.getId(),
            toVehicleDTO(register.getVehicle()),
            register.getEntrydate(),
            register.getExitdate(),
            register.getMinutes()
        );
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getRole()
        );
    }
}
