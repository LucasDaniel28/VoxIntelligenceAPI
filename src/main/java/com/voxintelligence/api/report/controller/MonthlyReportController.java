package com.voxintelligence.api.report.controller;

import com.voxintelligence.api.report.dto.MonthlyReportResponse;
import com.voxintelligence.api.report.service.MonthlyReportService;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class MonthlyReportController {
    private final MonthlyReportService service;
    public MonthlyReportController(MonthlyReportService service) { this.service=service; }

    @GetMapping("/monthly")
    public MonthlyReportResponse monthly(
            @RequestParam @Min(2000) @Max(9999) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return service.get(year, month);
    }
}
