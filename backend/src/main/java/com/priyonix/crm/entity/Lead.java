package com.priyonix.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==============================
    // LEAD NAME
    // ==============================

    @NotBlank(message = "Lead name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Lead name must be between 2 and 100 characters"
    )
    @Column(nullable = false)
    private String name;


    // ==============================
    // PHONE
    // ==============================

    @Pattern(
            regexp = "^[0-9+()\\-\\s]{7,20}$",
            message = "Please enter a valid phone number"
    )
    private String phone;


    // ==============================
    // EMAIL
    // ==============================

    @Email(
            message = "Please enter a valid email address"
    )
    private String email;


    // ==============================
    // OTHER DETAILS
    // ==============================

    private String company;

    private String location;

    private String source;

    private String requirement;


    // ==============================
    // ASSIGNED EMPLOYEE
    // ==============================

    private Long assignedUserId;


    // ==============================
    // PRIORITY
    // ==============================

    private String priority;


    // ==============================
    // STATUS
    // ==============================

    private String status;


    // ==============================
    // FOLLOW-UP DATE
    // ==============================

    private LocalDate followUpDate;


    // ==============================
    // NOTES
    // ==============================

    private String notes;


    // ==============================
    // CONSTRUCTOR
    // ==============================

    public Lead() {
    }


    // ==============================
    // GETTERS & SETTERS
    // ==============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }


    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(
            Long assignedUserId) {

        this.assignedUserId =
                assignedUserId;
    }


    public String getPriority() {
        return priority;
    }

    public void setPriority(
            String priority) {

        this.priority = priority;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }


    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(
            LocalDate followUpDate) {

        this.followUpDate =
                followUpDate;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes) {

        this.notes = notes;
    }
}