package com.parking.core.handlers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.service.ReportService;

@WebMvcTest(ReportHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET /monthly - should generate report successfully")
    void shouldGenerateReport() throws Exception {
        File mockFile = new File("./reports/informe_estacionamiento.txt");
        when(reportService.generateReportMonthly()).thenReturn(mockFile);

        mockMvc.perform(get("/api/v1/parking/reports/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Report created successfully"))
                .andExpect(jsonPath("$.report_file").exists());
    }

    @Test
    @DisplayName("GET /monthly - should return 400 when no resident vehicles")
    void shouldReturn400WhenNoResidents() throws Exception {
        when(reportService.generateReportMonthly())
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "There are not resident vehicles"));

        mockMvc.perform(get("/api/v1/parking/reports/monthly"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("There are not resident vehicles"));
    }
}
