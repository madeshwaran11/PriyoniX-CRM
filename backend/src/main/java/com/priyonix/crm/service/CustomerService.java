package com.priyonix.crm.service;

import com.priyonix.crm.entity.Customer;
import com.priyonix.crm.entity.User;
import com.priyonix.crm.repository.CustomerRepository;
import com.priyonix.crm.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            UserRepository userRepository) {

        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE CUSTOMER
    // =========================================================

    public Customer createCustomer(Customer customer) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // EMPLOYEE cannot create customers
        if ("EMPLOYEE".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "Employees cannot create customers"
            );
        }

        // SALES automatically owns the customer
        if ("SALES".equalsIgnoreCase(role)) {

            customer.setAssignedUserId(
                    currentUser.getId()
            );
        }

        // ADMIN / MANAGER can assign
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            if (customer.getAssignedUserId() != null) {

                validateAssignment(
                        customer.getAssignedUserId(),
                        role
                );
            }
        }

        // Default status
        if (customer.getStatus() == null
                || customer.getStatus().isBlank()) {

            customer.setStatus("ACTIVE");
        }

        // Default customer type
        if (customer.getCustomerType() == null
                || customer.getCustomerType().isBlank()) {

            customer.setCustomerType("REGULAR");
        }

        return customerRepository.save(customer);
    }

    // =========================================================
    // GET ALL CUSTOMERS
    // =========================================================

    public List<Customer> getAllCustomers() {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // ADMIN → all
        if ("ADMIN".equalsIgnoreCase(role)) {

            return customerRepository.findAll();
        }

        // MANAGER → all
        if ("MANAGER".equalsIgnoreCase(role)) {

            return customerRepository.findAll();
        }

        // SALES → own customers
        if ("SALES".equalsIgnoreCase(role)) {

            return customerRepository.findByAssignedUserId(
                    currentUser.getId()
            );
        }

        // EMPLOYEE → assigned customers
        return customerRepository.findByAssignedUserId(
                currentUser.getId()
        );
    }

    // =========================================================
    // GET CUSTOMER BY ID
    // =========================================================

    public Optional<Customer> getCustomerById(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Optional<Customer> customer =
                customerRepository.findById(id);

        if (customer.isEmpty()) {
            return Optional.empty();
        }

        // ADMIN / MANAGER → any customer
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            return customer;
        }

        // SALES / EMPLOYEE → assigned only
        if (!isOwnCustomer(
                customer.get(),
                currentUser.getId())) {

            throw new AccessDeniedException(
                    "You do not have permission to view this customer"
            );
        }

        return customer;
    }

    // =========================================================
    // UPDATE CUSTOMER
    // =========================================================

    public Customer updateCustomer(
            Long id,
            Customer updatedCustomer) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        Customer existingCustomer =
                customerRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Customer not found"
                                )
                        );

        // SALES / EMPLOYEE
        // can update only their assigned customers
        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {

            if (!isOwnCustomer(
                    existingCustomer,
                    currentUser.getId())) {

                throw new AccessDeniedException(
                        "You can only update your assigned customers"
                );
            }

            // Cannot reassign
            existingCustomer.setAssignedUserId(
                    currentUser.getId()
            );
        }

        // ADMIN / MANAGER can update assignment
        if ("ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {

            if (updatedCustomer.getAssignedUserId() != null) {

                validateAssignment(
                        updatedCustomer.getAssignedUserId(),
                        role
                );
            }

            existingCustomer.setAssignedUserId(
                    updatedCustomer.getAssignedUserId()
            );
        }

        // Common fields
        existingCustomer.setName(
                updatedCustomer.getName()
        );

        existingCustomer.setPhone(
                updatedCustomer.getPhone()
        );

        existingCustomer.setEmail(
                updatedCustomer.getEmail()
        );

        existingCustomer.setCompany(
                updatedCustomer.getCompany()
        );

        existingCustomer.setLocation(
                updatedCustomer.getLocation()
        );

        existingCustomer.setSource(
                updatedCustomer.getSource()
        );

        existingCustomer.setRequirement(
                updatedCustomer.getRequirement()
        );

        existingCustomer.setCustomerType(
                updatedCustomer.getCustomerType()
        );

        existingCustomer.setStatus(
                updatedCustomer.getStatus()
        );

        existingCustomer.setNotes(
                updatedCustomer.getNotes()
        );

        return customerRepository.save(
                existingCustomer
        );
    }

    // =========================================================
    // DELETE CUSTOMER
    // =========================================================

    public void deleteCustomer(Long id) {

        User currentUser = getCurrentUser();
        String role = currentUser.getRole();

        // Only ADMIN / MANAGER
        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "You do not have permission to delete customers"
            );
        }

        if (!customerRepository.existsById(id)) {

            throw new RuntimeException(
                    "Customer not found"
            );
        }

        customerRepository.deleteById(id);
    }

    // =========================================================
    // FILTER BY STATUS
    // =========================================================

    public List<Customer> getCustomersByStatus(
            String status) {

        return getAllCustomers()
                .stream()
                .filter(customer ->
                        customer.getStatus() != null
                                && customer.getStatus()
                                .equalsIgnoreCase(status)
                )
                .toList();
    }

    // =========================================================
    // FILTER BY CUSTOMER TYPE
    // =========================================================

    public List<Customer> getCustomersByType(
            String customerType) {

        return getAllCustomers()
                .stream()
                .filter(customer ->
                        customer.getCustomerType() != null
                                && customer.getCustomerType()
                                .equalsIgnoreCase(customerType)
                )
                .toList();
    }

    // =========================================================
    // SEARCH CUSTOMERS
    // =========================================================

    public List<Customer> searchCustomers(
            String name) {

        if (name == null || name.isBlank()) {

            return getAllCustomers();
        }

        String searchText =
                name.toLowerCase();

        return getAllCustomers()
                .stream()
                .filter(customer ->
                        customer.getName() != null
                                && customer.getName()
                                .toLowerCase()
                                .contains(searchText)
                )
                .toList();
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
    // CHECK OWNERSHIP
    // =========================================================

    private boolean isOwnCustomer(
            Customer customer,
            Long currentUserId) {

        return customer.getAssignedUserId() != null
                && customer.getAssignedUserId()
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
                    "Cannot assign customer to an inactive user"
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
                        "Manager can assign customers only to Sales or Employee"
                );
            }
        }
    }
}