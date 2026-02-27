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

    /**
     * Converts a {@link Vehicle} entity to a {@link VehicleDTO}.
     *
     * @param vehicle the vehicle entity
     * @return the corresponding DTO
     */
    public static VehicleDTO toVehicleDTO(Vehicle vehicle) {
        return new VehicleDTO(vehicle.getId(), vehicle.getType());
    }

    /**
     * Converts a {@link Register} entity to a {@link RegisterDTO},
     * including the nested {@link VehicleDTO}.
     *
     * @param register the register entity
     * @return the corresponding DTO
     */
    public static RegisterDTO toRegisterDTO(Register register) {
        return new RegisterDTO(
            register.getId(),
            toVehicleDTO(register.getVehicle()),
            register.getEntrydate(),
            register.getExitdate(),
            register.getMinutes()
        );
    }

    /**
     * Converts a {@link User} entity to a {@link UserDTO},
     * excluding sensitive fields like the password.
     *
     * @param user the user entity
     * @return the corresponding DTO
     */
    public static UserDTO toUserDTO(User user) {
        return new UserDTO(
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getRole()
        );
    }
}
