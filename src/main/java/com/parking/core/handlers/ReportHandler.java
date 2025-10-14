package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.service.ReportService;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("api/v1/parking/reports")
public class ReportHandler {
    private final ReportService reportService;

    public ReportHandler(ReportService reportService){
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getReportMonthly() {
        try {
            File file = reportService.generateReportMonthly();
            Map<String,Object> response = new LinkedHashMap<>();
            response.put("message", "report created successfully");
            response.put("report_file", file.getPath());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
        }
    }
    


}
