package com.priyonix.crm.repository;

import com.priyonix.crm.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByLeadId(Long leadId);

    List<Activity> findByCustomerId(Long customerId);

    List<Activity> findByUserId(Long userId);

    List<Activity> findByType(String type);
}