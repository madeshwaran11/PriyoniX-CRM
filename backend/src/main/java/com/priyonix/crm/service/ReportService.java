package com.priyonix.crm.service;

import com.priyonix.crm.entity.FollowUp;
import com.priyonix.crm.entity.Lead;
import com.priyonix.crm.entity.Task;
import com.priyonix.crm.entity.User;
import com.priyonix.crm.repository.FollowUpRepository;
import com.priyonix.crm.repository.LeadRepository;
import com.priyonix.crm.repository.TaskRepository;
import com.priyonix.crm.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final LeadRepository leadRepository;
    private final FollowUpRepository followUpRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ReportService(
            LeadRepository leadRepository,
            FollowUpRepository followUpRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {

        this.leadRepository = leadRepository;
        this.followUpRepository = followUpRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // LEAD SOURCE REPORT
    // =========================================================

    public Map<String, Long> getLeadSourceReport() {

        List<Lead> leads = leadRepository.findAll();

        Map<String, Long> report =
                new LinkedHashMap<>();

        for (Lead lead : leads) {

            String source = lead.getSource();

            if (source == null ||
                    source.trim().isEmpty()) {

                source = "Unknown";
            }

            report.put(
                    source,
                    report.getOrDefault(source, 0L) + 1
            );
        }

        return report;
    }


    // =========================================================
    // LEAD CONVERSION REPORT
    // =========================================================

    public Map<String, Long> getLeadConversionReport() {

        List<Lead> leads =
                leadRepository.findAll();

        long total =
                leads.size();

        long won =
                leads.stream()
                        .filter(lead ->
                                "WON".equalsIgnoreCase(
                                        safeStatus(
                                                lead.getStatus()
                                        )
                                )
                        )
                        .count();

        long lost =
                leads.stream()
                        .filter(lead ->
                                "LOST".equalsIgnoreCase(
                                        safeStatus(
                                                lead.getStatus()
                                        )
                                )
                        )
                        .count();

        long open =
                leads.stream()
                        .filter(lead -> {

                            String status =
                                    safeStatus(
                                            lead.getStatus()
                                    );

                            return !status.equals("WON")
                                    && !status.equals("LOST");
                        })
                        .count();

        Map<String, Long> report =
                new LinkedHashMap<>();

        report.put(
                "Total Leads",
                total
        );

        report.put(
                "Won",
                won
        );

        report.put(
                "Lost",
                lost
        );

        report.put(
                "Open",
                open
        );

        return report;
    }


    // =========================================================
    // PIPELINE REPORT
    // =========================================================

    public Map<String, Long> getPipelineReport() {

        List<Lead> leads =
                leadRepository.findAll();

        Map<String, Long> report =
                new LinkedHashMap<>();

        report.put(
                "NEW",
                countByStatus(
                        leads,
                        "NEW"
                )
        );

        report.put(
                "CONTACTED",
                countByStatus(
                        leads,
                        "CONTACTED"
                )
        );

        report.put(
                "QUALIFIED",
                countByStatus(
                        leads,
                        "QUALIFIED"
                )
        );

        report.put(
                "DISCUSSION",
                countByStatus(
                        leads,
                        "DISCUSSION"
                )
        );

        report.put(
                "PROPOSAL",
                countByStatus(
                        leads,
                        "PROPOSAL"
                )
        );

        report.put(
                "WON",
                countByStatus(
                        leads,
                        "WON"
                )
        );

        report.put(
                "LOST",
                countByStatus(
                        leads,
                        "LOST"
                )
        );

        return report;
    }


    // =========================================================
    // COUNT BY STATUS
    // =========================================================

    private long countByStatus(
            List<Lead> leads,
            String status) {

        return leads.stream()
                .filter(lead ->
                        status.equalsIgnoreCase(
                                safeStatus(
                                        lead.getStatus()
                                )
                        )
                )
                .count();
    }


    // =========================================================
    // FOLLOW-UP REPORT
    // =========================================================

    public Map<String, Long> getFollowUpReport() {

        List<FollowUp> followUps =
                followUpRepository.findAll();

        long total =
                followUps.size();

        long pending =
                followUps.stream()
                        .filter(followUp ->
                                "PENDING".equalsIgnoreCase(
                                        safeStatus(
                                                followUp.getStatus()
                                        )
                                )
                        )
                        .count();

        long completed =
                followUps.stream()
                        .filter(followUp ->
                                "COMPLETED".equalsIgnoreCase(
                                        safeStatus(
                                                followUp.getStatus()
                                        )
                                )
                        )
                        .count();

        long cancelled =
                followUps.stream()
                        .filter(followUp ->
                                "CANCELLED".equalsIgnoreCase(
                                        safeStatus(
                                                followUp.getStatus()
                                        )
                                )
                        )
                        .count();

        Map<String, Long> report =
                new LinkedHashMap<>();

        report.put(
                "Total",
                total
        );

        report.put(
                "Pending",
                pending
        );

        report.put(
                "Completed",
                completed
        );

        report.put(
                "Cancelled",
                cancelled
        );

        return report;
    }


    // =========================================================
    // EMPLOYEE PERFORMANCE
    // =========================================================

    public List<Map<String, Object>>
    getEmployeePerformance() {

        List<User> users =
                userRepository.findAll();

        List<Task> tasks =
                taskRepository.findAll();

        return users.stream()
                .map(user -> {

                    long totalTasks =
                            tasks.stream()
                                    .filter(task ->
                                            user.getId()
                                                    .equals(
                                                            task.getAssignedUserId()
                                                    )
                                    )
                                    .count();

                    long completedTasks =
                            tasks.stream()
                                    .filter(task ->
                                            user.getId()
                                                    .equals(
                                                            task.getAssignedUserId()
                                                    )
                                    )
                                    .filter(task ->
                                            "COMPLETED".equalsIgnoreCase(
                                                    safeStatus(
                                                            task.getStatus()
                                                    )
                                            )
                                    )
                                    .count();

                    long pendingTasks =
                            totalTasks -
                                    completedTasks;

                    Map<String, Object> result =
                            new LinkedHashMap<>();

                    result.put(
                            "userId",
                            user.getId()
                    );

                    result.put(
                            "name",
                            user.getName()
                    );

                    result.put(
                            "email",
                            user.getEmail()
                    );

                    result.put(
                            "role",
                            user.getRole()
                    );

                    result.put(
                            "totalTasks",
                            totalTasks
                    );

                    result.put(
                            "completedTasks",
                            completedTasks
                    );

                    result.put(
                            "pendingTasks",
                            pendingTasks
                    );

                    return result;
                })
                .collect(Collectors.toList());
    }


    // =========================================================
    // SAFE STATUS
    // =========================================================

    private String safeStatus(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return "";
        }

        return status
                .trim()
                .toUpperCase();
    }
}