package com.priyonix.crm.controller;

import com.priyonix.crm.entity.Customer;
import com.priyonix.crm.entity.Lead;
import com.priyonix.crm.service.LeadService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class LeadController {

    private final LeadService leadService;

    public LeadController(
            LeadService leadService) {

        this.leadService = leadService;
    }


    // ==============================
    // CREATE LEAD
    // ==============================

    @PostMapping
    public ResponseEntity<Lead> createLead(
            @Valid @RequestBody Lead lead) {

        return ResponseEntity.ok(
                leadService.createLead(lead)
        );
    }


    // ==============================
    // GET ALL LEADS
    // ==============================

    @GetMapping
    public ResponseEntity<List<Lead>> getAllLeads() {

        return ResponseEntity.ok(
                leadService.getAllLeads()
        );
    }


    // ==============================
    // GET LEAD BY ID
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<Lead> getLeadById(
            @PathVariable Long id) {

        return leadService
                .getLeadById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }


    // ==============================
    // UPDATE LEAD
    // ==============================

    @PutMapping("/{id}")
    public ResponseEntity<Lead> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody Lead lead) {

        return ResponseEntity.ok(
                leadService.updateLead(
                        id,
                        lead
                )
        );
    }


    // ==============================
    // DELETE LEAD
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(
            @PathVariable Long id) {

        leadService.deleteLead(id);

        return ResponseEntity
                .noContent()
                .build();
    }


    // ==============================
    // FILTER BY STATUS
    // ==============================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Lead>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                leadService.getLeadsByStatus(
                        status
                )
        );
    }


    // ==============================
    // FILTER BY PRIORITY
    // ==============================

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Lead>> getByPriority(
            @PathVariable String priority) {

        return ResponseEntity.ok(
                leadService.getLeadsByPriority(
                        priority
                )
        );
    }


    // ==============================
    // SEARCH
    // ==============================

    @GetMapping("/search")
    public ResponseEntity<List<Lead>> searchLeads(
            @RequestParam String name) {

        return ResponseEntity.ok(
                leadService.searchLeads(name)
        );
    }


    // ==============================
    // CONVERT LEAD TO CUSTOMER
    // ==============================

    @PostMapping("/{id}/convert")
    public ResponseEntity<Customer> convertLead(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                leadService.convertToCustomer(id)
        );
    }
}