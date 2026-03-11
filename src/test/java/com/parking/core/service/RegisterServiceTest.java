package com.parking.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.model.dto.RegisterEntryRequest;
import com.parking.core.payment.services.StripePaymentLinkService;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private RegisterRepository registerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ParkingService parkingService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private StripePaymentLinkService stripePaymentLinkService;

    @InjectMocks
    private RegisterService registerService;

    private Vehicle testVehicle;
    private RegisterEntryRequest testRequest;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle("ABC123", VehicleType.NON_RESIDENT);
        testRequest = new RegisterEntryRequest("ABC123", VehicleType.NON_RESIDENT, null, null);
    }

    @Nested
    @DisplayName("getAllRegisters")
    class GetAllRegistersTests {

        @Test
        @DisplayName("should return all registers")
        void shouldReturnAll() {
            Register r1 = new Register(testVehicle);
            r1.setEntrydate(LocalDateTime.now());
            when(registerRepository.findAll()).thenReturn(List.of(r1));

            List<Register> result = registerService.getAllRegisters();

            assertEquals(1, result.size());
            assertEquals(testVehicle, result.get(0).getVehicle());
        }

        @Test
        @DisplayName("should throw 404 when no registers")
        void shouldThrow404WhenEmpty() {
            when(registerRepository.findAll()).thenReturn(Collections.emptyList());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> registerService.getAllRegisters());

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("registerVehicleEntrance")
    class RegisterEntranceTests {

        @Test
        @DisplayName("should register vehicle entrance successfully")
        void shouldRegisterEntrance() {
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.of(testVehicle));
            when(registerRepository.existsByVehicleAndExitdateIsNull(testVehicle)).thenReturn(false);
            when(registerRepository.save(any(Register.class))).thenAnswer(invocation -> {
                Register r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            Register result = registerService.registerVehicleEntrance(testRequest);

            assertNotNull(result);
            assertEquals(testVehicle, result.getVehicle());
            assertNotNull(result.getEntrydate());
            verify(registerRepository).save(any(Register.class));
        }

        @Test
        @DisplayName("should throw 404 when vehicle not found")
        void shouldThrow404WhenVehicleNotFound() {
            RegisterEntryRequest unknownReq = new RegisterEntryRequest("UNKNOWN", VehicleType.OFICIAL, null, null);
            when(vehicleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> registerService.registerVehicleEntrance(unknownReq));

            assertEquals(404, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("UNKNOWN"));
        }

        @Test
        @DisplayName("should throw 400 when vehicle already has active register")
        void shouldThrow400WhenAlreadyRegistered() {
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.of(testVehicle));
            when(registerRepository.existsByVehicleAndExitdateIsNull(testVehicle)).thenReturn(true);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> registerService.registerVehicleEntrance(testRequest));

            assertEquals(400, ex.getStatusCode().value());
            verify(registerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set entry date on register")
        void shouldSetEntryDate() {
            when(vehicleRepository.findById("ABC123")).thenReturn(Optional.of(testVehicle));
            when(registerRepository.existsByVehicleAndExitdateIsNull(testVehicle)).thenReturn(false);
            when(registerRepository.save(any(Register.class))).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<Register> captor = ArgumentCaptor.forClass(Register.class);

            registerService.registerVehicleEntrance(testRequest);

            verify(registerRepository).save(captor.capture());
            assertNotNull(captor.getValue().getEntrydate());
            assertNull(captor.getValue().getExitdate());
        }
    }

    @Nested
    @DisplayName("leaveVehicle")
    class LeaveVehicleTests {

        @Test
        @DisplayName("should process vehicle departure and calculate minutes")
        void shouldProcessDeparture() {
            Register activeRegister = new Register(testVehicle);
            activeRegister.setId(1L);
            activeRegister.setEntrydate(LocalDateTime.now().minusMinutes(30));

            when(registerRepository.findByVehicleAndExitdateIsNull(testVehicle))
                    .thenReturn(Optional.of(activeRegister));
            when(registerRepository.save(any(Register.class))).thenAnswer(i -> i.getArgument(0));

            Register result = registerService.leaveVehicle(testVehicle);

            assertNotNull(result.getExitdate());
            assertTrue(result.getMinutes() >= 29); // allow small timing differences
            verify(registerRepository).save(activeRegister);
        }

        @Test
        @DisplayName("should throw 400 when no active register for vehicle")
        void shouldThrow400WhenNoActiveRegister() {
            when(registerRepository.findByVehicleAndExitdateIsNull(testVehicle))
                    .thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> registerService.leaveVehicle(testVehicle));

            assertEquals(400, ex.getStatusCode().value());
        }
    }
}
