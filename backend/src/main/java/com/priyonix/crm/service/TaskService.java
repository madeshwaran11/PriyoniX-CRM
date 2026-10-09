package com.priyonix.crm.service;

import com.priyonix.crm.entity.Task;
import com.priyonix.crm.entity.User;
import com.priyonix.crm.repository.TaskRepository;
import com.priyonix.crm.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE TASK
    // =========================================================

    public Task createTask(Task task) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // -----------------------------------------------------
        // ADMIN
        // Can assign to any active user
        // -----------------------------------------------------

        if ("ADMIN".equalsIgnoreCase(role)) {

            if (task.getAssignedUserId() != null) {
                validateActiveUser(task.getAssignedUserId());
            }
        }

        // -----------------------------------------------------
        // MANAGER
        // Can assign only to SALES / EMPLOYEE
        // -----------------------------------------------------

        else if ("MANAGER".equalsIgnoreCase(role)) {

            if (task.getAssignedUserId() != null) {
                validateManagerAssignment(
                        task.getAssignedUserId()
                );
            }
        }

        // -----------------------------------------------------
        // SALES / EMPLOYEE
        // Automatically assign to themselves
        // -----------------------------------------------------

        else if (
                "SALES".equalsIgnoreCase(role)
                        || "EMPLOYEE".equalsIgnoreCase(role)
        ) {

            task.setAssignedUserId(currentUser.getId());
        }

        else {
            throw new AccessDeniedException(
                    "You do not have permission to create tasks"
            );
        }

        // Default priority
        if (task.getPriority() == null
                || task.getPriority().isBlank()) {

            task.setPriority("MEDIUM");
        }

        // Default status
        if (task.getStatus() == null
                || task.getStatus().isBlank()) {

            task.setStatus("TODO");
        }

        return taskRepository.save(task);
    }

    // =========================================================
    // GET ALL TASKS
    // =========================================================

    public List<Task> getAllTasks() {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // ADMIN / MANAGER
        if (isAdminOrManager(role)) {
            return taskRepository.findAll();
        }

        // SALES / EMPLOYEE
        return taskRepository.findByAssignedUserId(
                currentUser.getId()
        );
    }

    // =========================================================
    // GET TASK BY ID
    // =========================================================

    public Optional<Task> getTaskById(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Optional<Task> task =
                taskRepository.findById(id);

        if (task.isEmpty()) {
            return Optional.empty();
        }

        // ADMIN / MANAGER
        if (isAdminOrManager(role)) {
            return task;
        }

        // SALES / EMPLOYEE
        if (!isOwnTask(
                task.get(),
                currentUser.getId()
        )) {

            throw new AccessDeniedException(
                    "You do not have permission to view this task"
            );
        }

        return task;
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    public Task updateTask(
            Long id,
            Task updatedTask) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Task existingTask =
                taskRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Task not found"
                                )
                        );

        // -----------------------------------------------------
        // SALES / EMPLOYEE
        // Can update only their own task
        // Cannot change assignment
        // -----------------------------------------------------

        if (
                "SALES".equalsIgnoreCase(role)
                        || "EMPLOYEE".equalsIgnoreCase(role)
        ) {

            if (!isOwnTask(
                    existingTask,
                    currentUser.getId()
            )) {

                throw new AccessDeniedException(
                        "You can only update your own tasks"
                );
            }

            existingTask.setAssignedUserId(
                    currentUser.getId()
            );
        }

        // -----------------------------------------------------
        // MANAGER
        // Can update team tasks
        // Can assign only SALES / EMPLOYEE
        // -----------------------------------------------------

        else if ("MANAGER".equalsIgnoreCase(role)) {

            if (updatedTask.getAssignedUserId() != null) {

                validateManagerAssignment(
                        updatedTask.getAssignedUserId()
                );

                existingTask.setAssignedUserId(
                        updatedTask.getAssignedUserId()
                );

            } else {

                existingTask.setAssignedUserId(null);
            }
        }

        // -----------------------------------------------------
        // ADMIN
        // Can update and assign anyone
        // -----------------------------------------------------

        else if ("ADMIN".equalsIgnoreCase(role)) {

            if (updatedTask.getAssignedUserId() != null) {

                validateActiveUser(
                        updatedTask.getAssignedUserId()
                );

                existingTask.setAssignedUserId(
                        updatedTask.getAssignedUserId()
                );

            } else {

                existingTask.setAssignedUserId(null);
            }
        }

        else {
            throw new AccessDeniedException(
                    "You do not have permission to update tasks"
            );
        }

        // -----------------------------------------------------
        // UPDATE TASK DETAILS
        // -----------------------------------------------------

        existingTask.setTitle(
                updatedTask.getTitle()
        );

        existingTask.setDescription(
                updatedTask.getDescription()
        );

        existingTask.setLeadId(
                updatedTask.getLeadId()
        );

        existingTask.setCustomerId(
                updatedTask.getCustomerId()
        );

        existingTask.setDueDate(
                updatedTask.getDueDate()
        );

        existingTask.setPriority(
                updatedTask.getPriority()
        );

        existingTask.setStatus(
                updatedTask.getStatus()
        );

        return taskRepository.save(existingTask);
    }

    // =========================================================
    // DELETE TASK
    // =========================================================

    public void deleteTask(Long id) {

        User currentUser = getCurrentUser();

        if (!isAdminOrManager(
                currentUser.getRole()
        )) {

            throw new AccessDeniedException(
                    "You do not have permission to delete tasks"
            );
        }

        if (!taskRepository.existsById(id)) {

            throw new RuntimeException(
                    "Task not found"
            );
        }

        taskRepository.deleteById(id);
    }

    // =========================================================
    // GET TASKS BY STATUS
    // =========================================================

    public List<Task> getTasksByStatus(
            String status) {

        User currentUser = getCurrentUser();

        if (isAdminOrManager(
                currentUser.getRole()
        )) {

            return taskRepository.findByStatus(
                    status
            );
        }

        return taskRepository
                .findByStatus(status)
                .stream()
                .filter(task ->
                        isOwnTask(
                                task,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // GET TASKS BY PRIORITY
    // =========================================================

    public List<Task> getTasksByPriority(
            String priority) {

        User currentUser = getCurrentUser();

        if (isAdminOrManager(
                currentUser.getRole()
        )) {

            return taskRepository.findByPriority(
                    priority
            );
        }

        return taskRepository
                .findByPriority(priority)
                .stream()
                .filter(task ->
                        isOwnTask(
                                task,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // GET TASKS BY ASSIGNED USER
    // =========================================================

    public List<Task> getTasksByAssignedUser(
            Long assignedUserId) {

        User currentUser = getCurrentUser();

        // ADMIN / MANAGER
        if (isAdminOrManager(
                currentUser.getRole()
        )) {

            return taskRepository
                    .findByAssignedUserId(
                            assignedUserId
                    );
        }

        // SALES / EMPLOYEE
        if (!currentUser.getId().equals(
                assignedUserId
        )) {

            throw new AccessDeniedException(
                    "You can only view your own tasks"
            );
        }

        return taskRepository
                .findByAssignedUserId(
                        currentUser.getId()
                );
    }

    // =========================================================
    // GET TASKS BY LEAD
    // =========================================================

    public List<Task> getTasksByLead(
            Long leadId) {

        User currentUser = getCurrentUser();

        if (isAdminOrManager(
                currentUser.getRole()
        )) {

            return taskRepository.findByLeadId(
                    leadId
            );
        }

        return taskRepository
                .findByLeadId(leadId)
                .stream()
                .filter(task ->
                        isOwnTask(
                                task,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // GET TASKS BY CUSTOMER
    // =========================================================

    public List<Task> getTasksByCustomer(
            Long customerId) {

        User currentUser = getCurrentUser();

        if (isAdminOrManager(
                currentUser.getRole()
        )) {

            return taskRepository
                    .findByCustomerId(
                            customerId
                    );
        }

        return taskRepository
                .findByCustomerId(customerId)
                .stream()
                .filter(task ->
                        isOwnTask(
                                task,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // GET TASKS BY DUE DATE
    // =========================================================

    public List<Task> getTasksByDueDate(
            LocalDate dueDate) {

        User currentUser = getCurrentUser();

        if (isAdminOrManager(
                currentUser.getRole()
        )) {

            return taskRepository.findByDueDate(
                    dueDate
            );
        }

        return taskRepository
                .findByDueDate(dueDate)
                .stream()
                .filter(task ->
                        isOwnTask(
                                task,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // GET CURRENT LOGGED-IN USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "User is not authenticated"
            );
        }

        String email =
                authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Current user not found"
                        )
                );
    }

    // =========================================================
    // CHECK ADMIN / MANAGER
    // =========================================================

    private boolean isAdminOrManager(
            String role) {

        return "ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role);
    }

    // =========================================================
    // CHECK TASK OWNERSHIP
    // =========================================================

    private boolean isOwnTask(
            Task task,
            Long currentUserId) {

        return task.getAssignedUserId() != null
                && task.getAssignedUserId()
                .equals(currentUserId);
    }

    // =========================================================
    // VALIDATE ACTIVE USER
    // =========================================================

    private void validateActiveUser(
            Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Assigned user not found"
                                )
                        );

        if (!user.isActive()) {

            throw new IllegalArgumentException(
                    "Cannot assign task to an inactive user"
            );
        }
    }

    // =========================================================
    // VALIDATE MANAGER ASSIGNMENT
    // =========================================================

    private void validateManagerAssignment(
            Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Assigned user not found"
                                )
                        );

        if (!user.isActive()) {

            throw new IllegalArgumentException(
                    "Cannot assign task to an inactive user"
            );
        }

        String assignedRole = user.getRole();

        if (!"SALES".equalsIgnoreCase(assignedRole)
                && !"EMPLOYEE".equalsIgnoreCase(assignedRole)) {

            throw new AccessDeniedException(
                    "Manager can assign tasks only to Sales or Employee"
            );
        }
    }
}