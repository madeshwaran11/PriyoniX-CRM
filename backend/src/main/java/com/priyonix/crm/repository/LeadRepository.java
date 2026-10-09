package com.priyonix.crm.repository;

import com.priyonix.crm.entity.Lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    // ==========================================
    // FILTER BY STATUS
    // ==========================================

    List<Lead> findByStatus(String status);


    // ==========================================
    // FILTER BY PRIORITY
    // ==========================================

    List<Lead> findByPriority(String priority);


    // ==========================================
    // SEARCH BY NAME
    // ==========================================

    List<Lead> findByNameContainingIgnoreCase(
            String name
    );


    // ==========================================
    // FIND LEADS BY ASSIGNED USER
    // ==========================================

    List<Lead> findByAssignedUserId(
            Long assignedUserId
    );
}