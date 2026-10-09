package com.priyonix.crm.repository;

import com.priyonix.crm.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    List<Customer> findByStatus(String status);

    List<Customer> findByCustomerType(String customerType);

    List<Customer> findByNameContainingIgnoreCase(String name);

    List<Customer> findByAssignedUserId(Long assignedUserId);
}