package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.service.ReportService;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * REST controller for report generation endpoints.
 * <p>
 * Base path: {@code /api/v1/parking/reports}
 * </p>
 *
 * @see ReportService
 */
@RestController
@RequestMapping("api/v1/parking/reports")
public class ReportHandler {
    private final ReportService reportService;

    public ReportHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Generates the monthly parking report for resident vehicles.
     * <p>
     * The report file is saved to {@code ./reports/informe_estacionamiento.txt}.
     * </p>
     *
     * @return {@code 200 OK} with a success message and the file path
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getReportMonthly() {
        File file = reportService.generateReportMonthly();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Report created successfully");
        response.put("report_file", file.getName());
        return ResponseEntity.ok(response);
    }
}
