package com.priyonix.crm.repository;

import com.priyonix.crm.entity.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FollowUpRepository
        extends JpaRepository<FollowUp, Long> {

    List<FollowUp> findByStatus(String status);

    List<FollowUp> findByFollowUpDate(LocalDate date);

    List<FollowUp> findByLeadId(Long leadId);

    List<FollowUp> findByCustomerId(Long customerId);

    List<FollowUp> findByAssignedUserId(Long assignedUserId);
}