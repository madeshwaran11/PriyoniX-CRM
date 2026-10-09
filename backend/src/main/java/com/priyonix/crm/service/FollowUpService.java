package com.priyonix.crm.service;

import com.priyonix.crm.entity.FollowUp;
import com.priyonix.crm.entity.Lead;
import com.priyonix.crm.entity.Customer;
import com.priyonix.crm.entity.User;
import com.priyonix.crm.repository.FollowUpRepository;
import com.priyonix.crm.repository.LeadRepository;
import com.priyonix.crm.repository.CustomerRepository;
import com.priyonix.crm.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;

    public FollowUpService(
            FollowUpRepository followUpRepository,
            UserRepository userRepository,
            LeadRepository leadRepository,
            CustomerRepository customerRepository) {

        this.followUpRepository = followUpRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
    }

    public FollowUp createFollowUp(FollowUp followUp) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        if ("EMPLOYEE".equalsIgnoreCase(role)) {
            throw new AccessDeniedException(
                    "Employees cannot create follow-ups"
            );
        }

        if ("SALES".equalsIgnoreCase(role)) {
            followUp.setAssignedUserId(
                    currentUser.getId()
            );
        }

        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            if (followUp.getAssignedUserId() != null) {
                validateAssignment(
                        followUp.getAssignedUserId(),
                        role
                );
            }
        }

        if (followUp.getStatus() == null
                || followUp.getStatus().isBlank()) {
            followUp.setStatus("PENDING");
        }

        if (followUp.getType() == null
                || followUp.getType().isBlank()) {
            followUp.setType("CALL");
        }

        return followUpRepository.save(followUp);
    }

    public List<FollowUp> getAllFollowUps() {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return enrichNames(followUpRepository.findAll());
        }

        return enrichNames(
                followUpRepository.findByAssignedUserId(
                        currentUser.getId()
                )
        );
    }

    public Optional<FollowUp> getFollowUpById(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Optional<FollowUp> followUp =
                followUpRepository.findById(id);

        if (followUp.isEmpty()) {
            return Optional.empty();
        }

        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            enrichNames(List.of(followUp.get()));
            return followUp;
        }

        if (!isOwnFollowUp(
                followUp.get(),
                currentUser.getId())) {

            throw new AccessDeniedException(
                    "You do not have permission to view this follow-up"
            );
        }

        enrichNames(List.of(followUp.get()));
        return followUp;
    }

    public FollowUp updateFollowUp(
            Long id,
            FollowUp updatedFollowUp) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        FollowUp existingFollowUp =
                followUpRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Follow-up not found"
                                )
                        );

        // Employee can update only an assigned follow-up.
        // Lead and Customer remain locked to the original follow-up.
        if ("EMPLOYEE".equalsIgnoreCase(role)) {

            if (!isOwnFollowUp(
                    existingFollowUp,
                    currentUser.getId())) {

                throw new AccessDeniedException(
                        "You can only update your assigned follow-ups"
                );
            }

            existingFollowUp.setAssignedUserId(
                    currentUser.getId()
            );

            // DO NOT update leadId or customerId for Employee.
            // They remain connected to the original Lead/Customer.

        } else {

            // Sales can update only their own follow-ups.
            if ("SALES".equalsIgnoreCase(role)) {

                if (!isOwnFollowUp(
                        existingFollowUp,
                        currentUser.getId())) {

                    throw new AccessDeniedException(
                            "You can only update your own follow-ups"
                    );
                }

                existingFollowUp.setAssignedUserId(
                        currentUser.getId()
                );
            }

            // Admin / Manager can update and reassign.
            if ("ADMIN".equalsIgnoreCase(role)
                    || "MANAGER".equalsIgnoreCase(role)) {

                if (updatedFollowUp.getAssignedUserId() != null) {

                    validateAssignment(
                            updatedFollowUp.getAssignedUserId(),
                            role
                    );
                }

                existingFollowUp.setAssignedUserId(
                        updatedFollowUp.getAssignedUserId()
                );

                existingFollowUp.setLeadId(
                        updatedFollowUp.getLeadId()
                );

                existingFollowUp.setCustomerId(
                        updatedFollowUp.getCustomerId()
                );
            }
        }

        // Everyone who is allowed to edit can update
        // the follow-up execution details.
        existingFollowUp.setFollowUpDate(
                updatedFollowUp.getFollowUpDate()
        );

        existingFollowUp.setFollowUpTime(
                updatedFollowUp.getFollowUpTime()
        );

        existingFollowUp.setOutcome(
                updatedFollowUp.getOutcome()
        );

        existingFollowUp.setNextFollowUp(
                updatedFollowUp.getNextFollowUp()
        );

        existingFollowUp.setType(
                updatedFollowUp.getType()
        );

        existingFollowUp.setStatus(
                updatedFollowUp.getStatus()
        );

        existingFollowUp.setNotes(
                updatedFollowUp.getNotes()
        );

        return followUpRepository.save(
                existingFollowUp
        );
    }

    public void deleteFollowUp(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "You do not have permission to delete follow-ups"
            );
        }

        if (!followUpRepository.existsById(id)) {

            throw new RuntimeException(
                    "Follow-up not found"
            );
        }

        followUpRepository.deleteById(id);
    }

    public List<FollowUp> getFollowUpsByStatus(
            String status) {

        return getAllFollowUps()
                .stream()
                .filter(followUp ->
                        followUp.getStatus() != null
                                && followUp.getStatus()
                                .equalsIgnoreCase(status)
                )
                .toList();
    }

    public List<FollowUp> getFollowUpsByDate(
            LocalDate date) {

        return getAllFollowUps()
                .stream()
                .filter(followUp ->
                        followUp.getFollowUpDate() != null
                                && followUp.getFollowUpDate()
                                .equals(date)
                )
                .toList();
    }

    public List<FollowUp> getFollowUpsByLead(
            Long leadId) {

        return getAllFollowUps()
                .stream()
                .filter(followUp ->
                        followUp.getLeadId() != null
                                && followUp.getLeadId()
                                .equals(leadId)
                )
                .toList();
    }

    public List<FollowUp> getFollowUpsByCustomer(
            Long customerId) {

        return getAllFollowUps()
                .stream()
                .filter(followUp ->
                        followUp.getCustomerId() != null
                                && followUp.getCustomerId()
                                .equals(customerId)
                )
                .toList();
    }

    private List<FollowUp> enrichNames(List<FollowUp> followUps) {

        for (FollowUp followUp : followUps) {

            if (followUp.getLeadId() != null) {
                leadRepository.findById(followUp.getLeadId())
                        .map(Lead::getName)
                        .ifPresent(followUp::setLeadName);
            }

            if (followUp.getCustomerId() != null) {
                customerRepository.findById(followUp.getCustomerId())
                        .map(Customer::getName)
                        .ifPresent(followUp::setCustomerName);
            }
        }

        return followUps;
    }

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

    private boolean isOwnFollowUp(
            FollowUp followUp,
            Long currentUserId) {

        return followUp.getAssignedUserId() != null
                && followUp.getAssignedUserId()
                .equals(currentUserId);
    }

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
                    "Cannot assign follow-up to an inactive user"
            );
        }

        if ("MANAGER".equalsIgnoreCase(currentRole)) {

            if (!"SALES".equalsIgnoreCase(
                    assignedUser.getRole())
                    && !"EMPLOYEE".equalsIgnoreCase(
                    assignedUser.getRole())) {

                throw new AccessDeniedException(
                        "Manager can assign follow-ups only to Sales or Employee"
                );
            }
        }
    }
}
