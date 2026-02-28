package com.parking.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private RegisterRepository registerRepository;

    @InjectMocks
    private ReportService reportService;

    @AfterEach
    void cleanUp() {
        File file = new File("./reports/informe_estacionamiento.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("should generate monthly report file")
    void shouldGenerateReport() throws Exception {
        Vehicle resident = new Vehicle("RES001", VehicleType.RESIDENT);
        Register r = new Register(resident);
        r.setMinutes(120);

        when(registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT)).thenReturn(List.of(r));

        File result = reportService.generateReportMonthly();

        assertTrue(result.exists());
        String content = Files.readString(result.toPath());
        assertTrue(content.contains("RES001"));
        assertTrue(content.contains("120"));
    }

    @Test
    @DisplayName("should throw 400 when no resident vehicles exist")
    void shouldThrow400WhenNoResidents() {
        when(registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT))
                .thenReturn(Collections.emptyList());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reportService.generateReportMonthly());

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("resident"));
    }

    @Test
    @DisplayName("should aggregate minutes per vehicle in report")
    void shouldAggregateMinutesPerVehicle() throws Exception {
        Vehicle resident = new Vehicle("RES001", VehicleType.RESIDENT);
        Register r1 = new Register(resident);
        r1.setMinutes(60);
        Register r2 = new Register(resident);
        r2.setMinutes(90);

        when(registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT)).thenReturn(List.of(r1, r2));

        File result = reportService.generateReportMonthly();

        assertTrue(result.exists());
        String content = Files.readString(result.toPath());
        assertTrue(content.contains("RES001"));
        // Total minutes should be 150 (60+90)
        assertTrue(content.contains("150"));
    }
}
