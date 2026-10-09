package com.priyonix.crm.controller;

import com.priyonix.crm.entity.Task;
import com.priyonix.crm.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class TaskController {

    private final TaskService taskService;

    public TaskController(
            TaskService taskService) {

        this.taskService = taskService;
    }

    // ==============================
    // CREATE TASK
    // ==============================

    @PostMapping
    public ResponseEntity<Task> createTask(
            @Valid @RequestBody Task task) {

        return ResponseEntity.ok(
                taskService.createTask(task)
        );
    }

    // ==============================
    // GET ALL TASKS
    // ==============================

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {

        return ResponseEntity.ok(
                taskService.getAllTasks()
        );
    }

    // ==============================
    // GET TASK BY ID
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable Long id) {

        return taskService
                .getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // ==============================
    // UPDATE TASK
    // ==============================

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task task) {

        return ResponseEntity.ok(
                taskService.updateTask(
                        id,
                        task
                )
        );
    }

    // ==============================
    // DELETE TASK
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    // ==============================
    // GET TASKS BY STATUS
    // ==============================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                taskService.getTasksByStatus(status)
        );
    }

    // ==============================
    // GET TASKS BY PRIORITY
    // ==============================

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getByPriority(
            @PathVariable String priority) {

        return ResponseEntity.ok(
                taskService.getTasksByPriority(priority)
        );
    }

    // ==============================
    // GET TASKS BY USER
    // ==============================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                taskService.getTasksByAssignedUser(userId)
        );
    }

    // ==============================
    // GET TASKS BY LEAD
    // ==============================

    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<Task>> getByLead(
            @PathVariable Long leadId) {

        return ResponseEntity.ok(
                taskService.getTasksByLead(leadId)
        );
    }

    // ==============================
    // GET TASKS BY CUSTOMER
    // ==============================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Task>> getByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                taskService.getTasksByCustomer(customerId)
        );
    }

    // ==============================
    // GET TASKS BY DATE
    // ==============================

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Task>> getByDate(
            @PathVariable LocalDate date) {

        return ResponseEntity.ok(
                taskService.getTasksByDueDate(date)
        );
    }
}