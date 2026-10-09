package com.priyonix.crm.controller;

import com.priyonix.crm.entity.FollowUp;
import com.priyonix.crm.service.FollowUpService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/follow-ups")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class FollowUpController {

    private final FollowUpService followUpService;

    public FollowUpController(
            FollowUpService followUpService) {

        this.followUpService = followUpService;
    }

    // ==============================
    // CREATE FOLLOW-UP
    // ==============================

    @PostMapping
    public ResponseEntity<FollowUp> createFollowUp(
            @Valid @RequestBody FollowUp followUp) {

        return ResponseEntity.ok(
                followUpService.createFollowUp(followUp)
        );
    }

    // ==============================
    // GET ALL FOLLOW-UPS
    // ==============================

    @GetMapping
    public ResponseEntity<List<FollowUp>> getAllFollowUps() {

        return ResponseEntity.ok(
                followUpService.getAllFollowUps()
        );
    }

    // ==============================
    // GET FOLLOW-UP BY ID
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<FollowUp> getFollowUpById(
            @PathVariable Long id) {

        return followUpService
                .getFollowUpById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // ==============================
    // UPDATE FOLLOW-UP
    // ==============================

    @PutMapping("/{id}")
    public ResponseEntity<FollowUp> updateFollowUp(
            @PathVariable Long id,
            @Valid @RequestBody FollowUp followUp) {

        return ResponseEntity.ok(
                followUpService.updateFollowUp(
                        id,
                        followUp
                )
        );
    }

    // ==============================
    // DELETE FOLLOW-UP
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFollowUp(
            @PathVariable Long id) {

        followUpService.deleteFollowUp(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    // ==============================
    // GET FOLLOW-UPS BY STATUS
    // ==============================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FollowUp>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                followUpService.getFollowUpsByStatus(status)
        );
    }

    // ==============================
    // GET FOLLOW-UPS BY DATE
    // ==============================

    @GetMapping("/date/{date}")
    public ResponseEntity<List<FollowUp>> getByDate(
            @PathVariable LocalDate date) {

        return ResponseEntity.ok(
                followUpService.getFollowUpsByDate(date)
        );
    }
}