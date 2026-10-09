package com.priyonix.crm.repository;

import com.priyonix.crm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(String status);

    List<Task> findByPriority(String priority);

    List<Task> findByAssignedUserId(Long assignedUserId);

    List<Task> findByLeadId(Long leadId);

    List<Task> findByCustomerId(Long customerId);

    List<Task> findByDueDate(LocalDate dueDate);
}