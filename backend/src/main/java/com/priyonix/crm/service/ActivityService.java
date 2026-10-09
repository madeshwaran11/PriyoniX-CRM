package com.priyonix.crm.service;

import com.priyonix.crm.entity.Activity;
import com.priyonix.crm.entity.User;
import com.priyonix.crm.repository.ActivityRepository;
import com.priyonix.crm.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(
            ActivityRepository activityRepository,
            UserRepository userRepository) {

        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE ACTIVITY
    // =========================================================

    public Activity createActivity(Activity activity) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // Sales and Employee create activities for themselves
        if ("SALES".equalsIgnoreCase(role)
                || "EMPLOYEE".equalsIgnoreCase(role)) {

            activity.setUserId(currentUser.getId());
        }

        // Admin / Manager can assign activities
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            if (activity.getUserId() != null) {
                validateAssignment(
                        activity.getUserId(),
                        role
                );
            } else {
                activity.setUserId(currentUser.getId());
            }
        }

        if (activity.getType() == null
                || activity.getType().isBlank()) {

            activity.setType("NOTE");
        }

        if (activity.getActivityDate() == null) {

            activity.setActivityDate(
                    LocalDateTime.now()
            );
        }

        return activityRepository.save(activity);
    }

    // =========================================================
    // GET ALL ACTIVITIES
    // =========================================================

    public List<Activity> getAllActivities() {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // Admin and Manager can see all
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return activityRepository.findAll();
        }

        // Sales and Employee see only their activities
        return activityRepository.findByUserId(
                currentUser.getId()
        );
    }

    // =========================================================
    // GET ACTIVITY BY ID
    // =========================================================

    public Optional<Activity> getActivityById(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Optional<Activity> activity =
                activityRepository.findById(id);

        if (activity.isEmpty()) {
            return Optional.empty();
        }

        // Admin / Manager can view any activity
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return activity;
        }

        // Sales / Employee can view only their activity
        if (!isOwnActivity(
                activity.get(),
                currentUser.getId())) {

            throw new AccessDeniedException(
                    "You do not have permission to view this activity"
            );
        }

        return activity;
    }

    // =========================================================
    // UPDATE ACTIVITY
    // =========================================================

    public Activity updateActivity(
            Long id,
            Activity updatedActivity) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Activity existingActivity =
                activityRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Activity not found"
                                )
                        );

        // Sales / Employee can update only their own activity
        if ("SALES".equalsIgnoreCase(role)
                || "EMPLOYEE".equalsIgnoreCase(role)) {

            if (!isOwnActivity(
                    existingActivity,
                    currentUser.getId())) {

                throw new AccessDeniedException(
                        "You can only update your own activities"
                );
            }

            // Keep ownership fixed
            existingActivity.setUserId(
                    currentUser.getId()
            );
        }

        // Admin / Manager can update and reassign
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            if (updatedActivity.getUserId() != null) {

                validateAssignment(
                        updatedActivity.getUserId(),
                        role
                );

                existingActivity.setUserId(
                        updatedActivity.getUserId()
                );
            }
        }

        // Update activity information
        existingActivity.setLeadId(
                updatedActivity.getLeadId()
        );

        existingActivity.setCustomerId(
                updatedActivity.getCustomerId()
        );

        existingActivity.setType(
                updatedActivity.getType()
        );

        existingActivity.setSubject(
                updatedActivity.getSubject()
        );

        existingActivity.setDescription(
                updatedActivity.getDescription()
        );

        existingActivity.setActivityDate(
                updatedActivity.getActivityDate()
        );

        return activityRepository.save(
                existingActivity
        );
    }

    // =========================================================
    // DELETE ACTIVITY
    // =========================================================

    public void deleteActivity(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // Only Admin and Manager can delete
        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "You do not have permission to delete activities"
            );
        }

        if (!activityRepository.existsById(id)) {

            throw new RuntimeException(
                    "Activity not found"
            );
        }

        activityRepository.deleteById(id);
    }

    // =========================================================
    // GET ACTIVITIES BY LEAD
    // =========================================================

    public List<Activity> getActivitiesByLead(
            Long leadId) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return activityRepository.findByLeadId(
                    leadId
            );
        }

        return activityRepository
                .findByLeadId(leadId)
                .stream()
                .filter(activity ->
                        isOwnActivity(
                                activity,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // GET ACTIVITIES BY CUSTOMER
    // =========================================================

    public List<Activity> getActivitiesByCustomer(
            Long customerId) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return activityRepository.findByCustomerId(
                    customerId
            );
        }

        return activityRepository
                .findByCustomerId(customerId)
                .stream()
                .filter(activity ->
                        isOwnActivity(
                                activity,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // GET ACTIVITIES BY USER
    // =========================================================

    public List<Activity> getActivitiesByUser(
            Long userId) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // Admin / Manager can view any user's activities
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return activityRepository.findByUserId(
                    userId
            );
        }

        // Sales / Employee can only view their own
        if (!currentUser.getId().equals(userId)) {

            throw new AccessDeniedException(
                    "You can only view your own activities"
            );
        }

        return activityRepository.findByUserId(
                currentUser.getId()
        );
    }

    // =========================================================
    // GET ACTIVITIES BY TYPE
    // =========================================================

    public List<Activity> getActivitiesByType(
            String type) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        List<Activity> activities =
                activityRepository.findByType(type);

        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return activities;
        }

        return activities
                .stream()
                .filter(activity ->
                        isOwnActivity(
                                activity,
                                currentUser.getId()
                        )
                )
                .toList();
    }

    // =========================================================
    // CURRENT USER
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

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Current user not found"
                        )
                );
    }

    // =========================================================
    // OWNERSHIP CHECK
    // =========================================================

    private boolean isOwnActivity(
            Activity activity,
            Long currentUserId) {

        return activity.getUserId() != null
                && activity.getUserId()
                .equals(currentUserId);
    }

    // =========================================================
    // ASSIGNMENT VALIDATION
    // =========================================================

    private void validateAssignment(
            Long assignedUserId,
            String currentRole) {

        User assignedUser =
                userRepository
                        .findById(assignedUserId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Assigned user not found"
                                )
                        );

        if (!assignedUser.isActive()) {

            throw new IllegalArgumentException(
                    "Cannot assign activity to an inactive user"
            );
        }

        // Manager can assign only to Sales / Employee
        if ("MANAGER".equalsIgnoreCase(currentRole)) {

            if (!"SALES".equalsIgnoreCase(
                    assignedUser.getRole())
                    && !"EMPLOYEE".equalsIgnoreCase(
                    assignedUser.getRole())) {

                throw new AccessDeniedException(
                        "Manager can assign activities only to Sales or Employee"
                );
            }
        }
    }
}