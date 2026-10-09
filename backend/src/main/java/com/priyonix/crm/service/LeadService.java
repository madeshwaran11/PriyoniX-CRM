package com.priyonix.crm.service;

import com.priyonix.crm.entity.Customer;
import com.priyonix.crm.entity.Lead;
import com.priyonix.crm.entity.User;
import com.priyonix.crm.repository.CustomerRepository;
import com.priyonix.crm.repository.LeadRepository;
import com.priyonix.crm.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public LeadService(
            LeadRepository leadRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository) {

        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE LEAD
    // =========================================================

    public Lead createLead(Lead lead) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // EMPLOYEE cannot create leads
        if ("EMPLOYEE".equalsIgnoreCase(role)) {
            throw new AccessDeniedException(
                    "Employees cannot create leads"
            );
        }

        // SALES can create only for themselves
        if ("SALES".equalsIgnoreCase(role)) {
            lead.setAssignedUserId(currentUser.getId());
        }

        // ADMIN / MANAGER can choose assignment
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            if (lead.getAssignedUserId() != null) {
                validateAssignment(
                        lead.getAssignedUserId(),
                        role
                );
            }
        }

        // Default status
        if (lead.getStatus() == null
                || lead.getStatus().isBlank()) {

            lead.setStatus("NEW");
        }

        // Default priority
        if (lead.getPriority() == null
                || lead.getPriority().isBlank()) {

            lead.setPriority("MEDIUM");
        }

        return leadRepository.save(lead);
    }

    // =========================================================
    // GET ALL LEADS
    // =========================================================

    public List<Lead> getAllLeads() {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // ADMIN → all leads
        if ("ADMIN".equalsIgnoreCase(role)) {
            return leadRepository.findAll();
        }

        // MANAGER → all leads
        if ("MANAGER".equalsIgnoreCase(role)) {
            return leadRepository.findAll();
        }

        // SALES → only assigned leads
        if ("SALES".equalsIgnoreCase(role)) {
            return leadRepository.findByAssignedUserId(
                    currentUser.getId()
            );
        }

        // EMPLOYEE → only assigned leads
        return leadRepository.findByAssignedUserId(
                currentUser.getId()
        );
    }

    // =========================================================
    // GET LEAD BY ID
    // =========================================================

    public Optional<Lead> getLeadById(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Optional<Lead> lead =
                leadRepository.findById(id);

        if (lead.isEmpty()) {
            return Optional.empty();
        }

        // ADMIN / MANAGER → any lead
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return lead;
        }

        // SALES / EMPLOYEE → assigned leads only
        if (!isOwnLead(
                lead.get(),
                currentUser.getId())) {

            throw new AccessDeniedException(
                    "You do not have permission to view this lead"
            );
        }

        return lead;
    }

    // =========================================================
    // UPDATE LEAD
    // =========================================================

    public Lead updateLead(
            Long id,
            Lead updatedLead) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Lead existingLead =
                leadRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Lead not found"
                                )
                        );

        // SALES / EMPLOYEE
        // Can update only their assigned lead
        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {

            if (!isOwnLead(
                    existingLead,
                    currentUser.getId())) {

                throw new AccessDeniedException(
                        "You can only update your assigned leads"
                );
            }
        }

        // Update common fields
        existingLead.setName(
                updatedLead.getName()
        );

        existingLead.setPhone(
                updatedLead.getPhone()
        );

        existingLead.setEmail(
                updatedLead.getEmail()
        );

        existingLead.setCompany(
                updatedLead.getCompany()
        );

        existingLead.setLocation(
                updatedLead.getLocation()
        );

        existingLead.setSource(
                updatedLead.getSource()
        );

        existingLead.setRequirement(
                updatedLead.getRequirement()
        );

        existingLead.setPriority(
                updatedLead.getPriority()
        );

        existingLead.setStatus(
                updatedLead.getStatus()
        );

        existingLead.setFollowUpDate(
                updatedLead.getFollowUpDate()
        );

        existingLead.setNotes(
                updatedLead.getNotes()
        );

        // ADMIN / MANAGER → assignment can change
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            if (updatedLead.getAssignedUserId() != null) {

                validateAssignment(
                        updatedLead.getAssignedUserId(),
                        role
                );
            }

            existingLead.setAssignedUserId(
                    updatedLead.getAssignedUserId()
            );

        } else {

            // SALES / EMPLOYEE cannot reassign
            existingLead.setAssignedUserId(
                    currentUser.getId()
            );
        }

        return leadRepository.save(existingLead);
    }

    // =========================================================
    // DELETE LEAD
    // =========================================================

    public void deleteLead(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // Only ADMIN / MANAGER
        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "You do not have permission to delete leads"
            );
        }

        if (!leadRepository.existsById(id)) {
            throw new RuntimeException(
                    "Lead not found"
            );
        }

        leadRepository.deleteById(id);
    }

    // =========================================================
    // FILTER BY STATUS
    // =========================================================

    public List<Lead> getLeadsByStatus(
            String status) {

        return getAllLeads()
                .stream()
                .filter(lead ->
                        lead.getStatus() != null
                                && lead.getStatus()
                                .equalsIgnoreCase(status)
                )
                .toList();
    }

    // =========================================================
    // FILTER BY PRIORITY
    // =========================================================

    public List<Lead> getLeadsByPriority(
            String priority) {

        return getAllLeads()
                .stream()
                .filter(lead ->
                        lead.getPriority() != null
                                && lead.getPriority()
                                .equalsIgnoreCase(priority)
                )
                .toList();
    }

    // =========================================================
    // SEARCH LEADS
    // =========================================================

    public List<Lead> searchLeads(
            String name) {

        if (name == null || name.isBlank()) {
            return getAllLeads();
        }

        String searchText =
                name.toLowerCase();

        return getAllLeads()
                .stream()
                .filter(lead ->
                        lead.getName() != null
                                && lead.getName()
                                .toLowerCase()
                                .contains(searchText)
                )
                .toList();
    }

    // =========================================================
    // CONVERT LEAD TO CUSTOMER
    // =========================================================

    public Customer convertToCustomer(
            Long leadId) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Lead lead =
                leadRepository.findById(leadId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Lead not found"
                                )
                        );

        // SALES / EMPLOYEE can convert
        // only their assigned lead
        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {

            if (!isOwnLead(
                    lead,
                    currentUser.getId())) {

                throw new AccessDeniedException(
                        "You can only convert your assigned leads"
                );
            }
        }

        Customer customer = new Customer();

        customer.setName(
                lead.getName()
        );

        customer.setPhone(
                lead.getPhone()
        );

        customer.setEmail(
                lead.getEmail()
        );

        customer.setCompany(
                lead.getCompany()
        );

        customer.setLocation(
                lead.getLocation()
        );

        customer.setSource(
                lead.getSource()
        );

        customer.setRequirement(
                lead.getRequirement()
        );

        customer.setCustomerType(
                "REGULAR"
        );

        customer.setStatus(
                "ACTIVE"
        );

        customer.setNotes(
                lead.getNotes()
        );

        // Preserve ownership
        customer.setAssignedUserId(
                lead.getAssignedUserId()
        );

        Customer savedCustomer =
                customerRepository.save(
                        customer
                );

        // Mark lead as WON
        lead.setStatus("WON");

        leadRepository.save(lead);

        return savedCustomer;
    }

    // =========================================================
    // GET CURRENT USER
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
    // CHECK LEAD OWNERSHIP
    // =========================================================

    private boolean isOwnLead(
            Lead lead,
            Long currentUserId) {

        return lead.getAssignedUserId() != null
                && lead.getAssignedUserId()
                .equals(currentUserId);
    }

    // =========================================================
    // VALIDATE ASSIGNMENT
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

        // Cannot assign inactive user
        if (!assignedUser.isActive()) {

            throw new IllegalArgumentException(
                    "Cannot assign lead to an inactive user"
            );
        }

        // MANAGER → SALES / EMPLOYEE only
        if ("MANAGER".equalsIgnoreCase(
                currentRole)) {

            if (!"SALES".equalsIgnoreCase(
                    assignedUser.getRole())
                    && !"EMPLOYEE".equalsIgnoreCase(
                    assignedUser.getRole())) {

                throw new AccessDeniedException(
                        "Manager can assign leads only to Sales or Employee"
                );
            }
        }
    }
}