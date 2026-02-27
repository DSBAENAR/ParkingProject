package com.parking.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RegisterRepository registerRepository;

    @InjectMocks
    private ParkingService parkingService;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle("ABC123", VehicleType.NON_RESIDENT);
    }

    @Nested
    @DisplayName("getVehicle")
    class GetVehicleTests {

        @Test
        @DisplayName("should return vehicle when exists")
        void shouldReturnVehicle() {
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.of(testVehicle));

            Vehicle result = parkingService.getVehicle(testVehicle);

            assertEquals("ABC123", result.getId());
            assertEquals(VehicleType.NON_RESIDENT, result.getType());
        }

        @Test
        @DisplayName("should throw 404 when vehicle not found")
        void shouldThrow404() {
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> parkingService.getVehicle(testVehicle));

            assertEquals(404, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("ABC123"));
        }
    }

    @Nested
    @DisplayName("getAllVehicles")
    class GetAllVehiclesTests {

        @Test
        @DisplayName("should return all vehicles")
        void shouldReturnAll() {
            Vehicle v2 = new Vehicle("DEF456", VehicleType.RESIDENT);
            when(vehicleRepository.findAll()).thenReturn(List.of(testVehicle, v2));

            List<Vehicle> result = parkingService.getAllVehicles();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should throw 404 when no vehicles")
        void shouldThrow404WhenEmpty() {
            when(vehicleRepository.findAll()).thenReturn(Collections.emptyList());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> parkingService.getAllVehicles());

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("calculatePayment")
    class CalculatePaymentTests {

        @Test
        @DisplayName("should calculate payment for NON_RESIDENT at 0.5/min")
        void shouldCalculateNonResident() {
            Register r = new Register(testVehicle);
            r.setMinutes(120);
            when(registerRepository.findAllByVehicle(testVehicle)).thenReturn(List.of(r));

            double payment = parkingService.calculatePayment(testVehicle);

            assertEquals(60.0, payment, 0.01);
        }

        @Test
        @DisplayName("should calculate payment for RESIDENT at 0.05/min")
        void shouldCalculateResident() {
            Vehicle resident = new Vehicle("RES001", VehicleType.RESIDENT);
            Register r = new Register(resident);
            r.setMinutes(200);
            when(registerRepository.findAllByVehicle(resident)).thenReturn(List.of(r));

            double payment = parkingService.calculatePayment(resident);

            assertEquals(10.0, payment, 0.01);
        }

        @Test
        @DisplayName("should return 0 for OFICIAL vehicles")
        void shouldReturnZeroForOficial() {
            Vehicle oficial = new Vehicle("OFC001", VehicleType.OFICIAL);
            Register r = new Register(oficial);
            r.setMinutes(500);
            when(registerRepository.findAllByVehicle(oficial)).thenReturn(List.of(r));

            double payment = parkingService.calculatePayment(oficial);

            assertEquals(0.0, payment, 0.01);
        }

        @Test
        @DisplayName("should sum minutes across multiple registers")
        void shouldSumMinutesAcrossRegisters() {
            Register r1 = new Register(testVehicle);
            r1.setMinutes(60);
            Register r2 = new Register(testVehicle);
            r2.setMinutes(40);
            when(registerRepository.findAllByVehicle(testVehicle)).thenReturn(List.of(r1, r2));

            double payment = parkingService.calculatePayment(testVehicle);

            assertEquals(50.0, payment, 0.01); // (60+40) * 0.5
        }

        @Test
        @DisplayName("should throw 400 when no registers found for vehicle")
        void shouldThrow400WhenNoRegisters() {
            when(registerRepository.findAllByVehicle(testVehicle)).thenReturn(Collections.emptyList());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> parkingService.calculatePayment(testVehicle));

            assertEquals(400, ex.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("saveVehicle")
    class SaveVehicleTests {

        @Test
        @DisplayName("should save new vehicle")
        void shouldSaveNewVehicle() {
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.empty());
            when(vehicleRepository.save(testVehicle)).thenReturn(testVehicle);

            Vehicle result = parkingService.saveVehicle(testVehicle);

            assertEquals("ABC123", result.getId());
            verify(vehicleRepository).save(testVehicle);
        }

        @Test
        @DisplayName("should throw 400 when vehicle already exists")
        void shouldThrow400WhenExists() {
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.of(testVehicle));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> parkingService.saveVehicle(testVehicle));

            assertEquals(400, ex.getStatusCode().value());
            verify(vehicleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("monthStarts")
    class MonthStartsTests {

        @Test
        @DisplayName("should delete official registers and reset resident minutes")
        void shouldResetMonth() {
            Register oficialReg = new Register(new Vehicle("OFC1", VehicleType.OFICIAL));
            oficialReg.setMinutes(100);
            Register residentReg = new Register(new Vehicle("RES1", VehicleType.RESIDENT));
            residentReg.setMinutes(200);

            when(registerRepository.findAllByVehicle_Type(VehicleType.OFICIAL))
                    .thenReturn(List.of(oficialReg));
            when(registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT))
                    .thenReturn(List.of(residentReg));

            Map<String, Object> result = parkingService.monthStarts();

            assertEquals(1, result.get("deletedCount"));
            assertEquals(0, residentReg.getMinutes());
            verify(registerRepository).saveAll(List.of(residentReg));
            verify(registerRepository).deleteAll(List.of(oficialReg));
        }

        @Test
        @DisplayName("should handle empty lists gracefully")
        void shouldHandleEmptyLists() {
            when(registerRepository.findAllByVehicle_Type(VehicleType.OFICIAL))
                    .thenReturn(Collections.emptyList());
            when(registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT))
                    .thenReturn(Collections.emptyList());

            Map<String, Object> result = parkingService.monthStarts();

            assertEquals(0, result.get("deletedCount"));
        }
    }

    @Nested
    @DisplayName("updateVehicle")
    class UpdateVehicleTests {

        @Test
        @DisplayName("should update vehicle type")
        void shouldUpdateVehicle() {
            Vehicle toUpdate = new Vehicle("ABC123", VehicleType.RESIDENT);
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.of(testVehicle));
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

            Vehicle result = parkingService.updateVehicle("ABC123", toUpdate);

            assertEquals(VehicleType.RESIDENT, result.getType());
            verify(vehicleRepository).save(testVehicle);
        }

        @Test
        @DisplayName("should throw 404 when vehicle not found")
        void shouldThrow404WhenNotFound() {
            when(vehicleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> parkingService.updateVehicle("UNKNOWN", testVehicle));

            assertEquals(404, ex.getStatusCode().value());
        }
    }
}
