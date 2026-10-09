package com.priyonix.crm.controller;

import com.priyonix.crm.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private final ReportService reportService;

    public ReportController(
            ReportService reportService) {

        this.reportService = reportService;
    }


    // Lead Source Report

    @GetMapping("/lead-source")
    public ResponseEntity<Map<String, Long>>
    getLeadSourceReport() {

        return ResponseEntity.ok(
                reportService.getLeadSourceReport()
        );
    }


    // Lead Conversion Report

    @GetMapping("/conversion")
    public ResponseEntity<Map<String, Long>>
    getLeadConversionReport() {

        return ResponseEntity.ok(
                reportService.getLeadConversionReport()
        );
    }


    // Pipeline Report

    @GetMapping("/pipeline")
    public ResponseEntity<Map<String, Long>>
    getPipelineReport() {

        return ResponseEntity.ok(
                reportService.getPipelineReport()
        );
    }


    // Follow-Up Report

    @GetMapping("/follow-ups")
    public ResponseEntity<Map<String, Long>>
    getFollowUpReport() {

        return ResponseEntity.ok(
                reportService.getFollowUpReport()
        );
    }


    // Employee Performance

    @GetMapping("/employee-performance")
    public ResponseEntity<List<Map<String, Object>>>
    getEmployeePerformance() {

        return ResponseEntity.ok(
                reportService.getEmployeePerformance()
        );
    }
}
