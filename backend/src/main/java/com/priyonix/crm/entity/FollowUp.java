package com.priyonix.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "follow_ups")
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leadId;
    private Long customerId;
    private Long assignedUserId;

    @NotNull(message = "Follow-up date is required")
    private LocalDate followUpDate;

    private LocalTime followUpTime;

    @Size(max = 50, message = "Follow-up type must not exceed 50 characters")
    private String type;

    @Size(max = 100, message = "Outcome must not exceed 100 characters")
    private String outcome;

    private LocalDate nextFollowUp;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Display-only fields. They are not stored in the follow_ups table.
    @Transient
    private String leadName;

    @Transient
    private String customerName;

    public FollowUp() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }

    public LocalTime getFollowUpTime() { return followUpTime; }
    public void setFollowUpTime(LocalTime followUpTime) { this.followUpTime = followUpTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public LocalDate getNextFollowUp() { return nextFollowUp; }
    public void setNextFollowUp(LocalDate nextFollowUp) { this.nextFollowUp = nextFollowUp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getLeadName() { return leadName; }
    public void setLeadName(String leadName) { this.leadName = leadName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
