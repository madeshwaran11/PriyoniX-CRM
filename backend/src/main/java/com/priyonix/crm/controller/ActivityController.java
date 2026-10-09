package com.priyonix.crm.controller;

import com.priyonix.crm.entity.Activity;
import com.priyonix.crm.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "http://localhost:5173")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<Activity> createActivity(
            @RequestBody Activity activity) {

        return ResponseEntity.ok(
                activityService.createActivity(activity)
        );
    }

    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {

        return ResponseEntity.ok(
                activityService.getAllActivities()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(
            @PathVariable Long id) {

        return activityService.getActivityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(
            @PathVariable Long id,
            @RequestBody Activity activity) {

        return ResponseEntity.ok(
                activityService.updateActivity(id, activity)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable Long id) {

        activityService.deleteActivity(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<Activity>> getActivitiesByLead(
            @PathVariable Long leadId) {

        return ResponseEntity.ok(
                activityService.getActivitiesByLead(leadId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Activity>> getActivitiesByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                activityService.getActivitiesByCustomer(customerId)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Activity>> getActivitiesByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                activityService.getActivitiesByUser(userId)
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Activity>> getActivitiesByType(
            @PathVariable String type) {

        return ResponseEntity.ok(
                activityService.getActivitiesByType(type)
        );
    }
}