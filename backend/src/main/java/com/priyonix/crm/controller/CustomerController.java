package com.priyonix.crm.controller;

import com.priyonix.crm.entity.Customer;
import com.priyonix.crm.service.CustomerService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    // ==============================
    // CREATE CUSTOMER
    // ==============================

    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @Valid @RequestBody Customer customer) {

        return ResponseEntity.ok(
                customerService.createCustomer(customer)
        );
    }

    // ==============================
    // GET ALL CUSTOMERS
    // ==============================

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {

        return ResponseEntity.ok(
                customerService.getAllCustomers()
        );
    }

    // ==============================
    // GET CUSTOMER BY ID
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @PathVariable Long id) {

        return customerService
                .getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // ==============================
    // UPDATE CUSTOMER
    // ==============================

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer) {

        return ResponseEntity.ok(
                customerService.updateCustomer(
                        id,
                        customer
                )
        );
    }

    // ==============================
    // DELETE CUSTOMER
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id) {

        customerService.deleteCustomer(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    // ==============================
    // GET CUSTOMERS BY STATUS
    // ==============================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Customer>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                customerService.getCustomersByStatus(status)
        );
    }

    // ==============================
    // GET CUSTOMERS BY TYPE
    // ==============================

    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<Customer>> getByCustomerType(
            @PathVariable String customerType) {

        return ResponseEntity.ok(
                customerService.getCustomersByType(customerType)
        );
    }

    // ==============================
    // SEARCH CUSTOMERS
    // ==============================

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam String name) {

        return ResponseEntity.ok(
                customerService.searchCustomers(name)
        );
    }
}