package com.parking.core.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parking.core.enums.Roles;
import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.User;
import com.parking.core.model.Vehicle;

class DtoMapperTest {

    @Test
    @DisplayName("toVehicleDTO - maps Vehicle correctly")
    void toVehicleDTO() {
        Vehicle vehicle = new Vehicle("ABC-123", VehicleType.RESIDENT);
        VehicleDTO dto = DtoMapper.toVehicleDTO(vehicle);

        assertEquals("ABC-123", dto.id());
        assertEquals(VehicleType.RESIDENT, dto.type());
    }

    @Test
    @DisplayName("toRegisterDTO - maps Register correctly")
    void toRegisterDTO() {
        Vehicle vehicle = new Vehicle("DEF-456", VehicleType.NON_RESIDENT);
        Register register = new Register(vehicle);
        register.setId(1L);
        register.setEntrydate(LocalDateTime.of(2025, 1, 15, 10, 0));
        register.setExitdate(LocalDateTime.of(2025, 1, 15, 12, 0));
        register.setMinutes(120);
        register.setPhoneNumber("+573001234567");

        RegisterDTO dto = DtoMapper.toRegisterDTO(register);

        assertEquals(1L, dto.id());
        assertEquals("DEF-456", dto.vehicle().id());
        assertEquals(VehicleType.NON_RESIDENT, dto.vehicle().type());
        assertEquals(120, dto.minutes());
        assertEquals("+573001234567", dto.phoneNumber());
        assertNotNull(dto.entryDate());
        assertNotNull(dto.exitDate());
    }

    @Test
    @DisplayName("toUserDTO - maps User correctly (excludes password)")
    void toUserDTO() {
        User user = new User("John Doe", "johndoe", Roles.ADMIN, "john@test.com", null);
        user.setPassword("secret");

        UserDTO dto = DtoMapper.toUserDTO(user);

        assertEquals("John Doe", dto.name());
        assertEquals("johndoe", dto.username());
        assertEquals("john@test.com", dto.email());
        assertEquals(Roles.ADMIN, dto.role());
    }
}
