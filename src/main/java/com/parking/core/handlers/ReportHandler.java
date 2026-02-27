package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.service.ReportService;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/v1/parking/reports")
public class ReportHandler {
    private final ReportService reportService;

    public ReportHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getReportMonthly() {
        File file = reportService.generateReportMonthly();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Report created successfully");
        response.put("report_file", file.getPath());
        return ResponseEntity.ok(response);
    }
}
