package com.sales.leadqualifier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object representing the request body for lead analysis.
 * Contains field validations using Jakarta Validation constraints.
 */
public class LeadAnalysisRequestDto {

    @NotBlank(message = "Lead name is required")
    private String leadName;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Industry selection is required")
    private String industry;

    @NotBlank(message = "Job role is required")
    private String jobRole;

    @NotNull(message = "Company size is required")
    @Min(value = 1, message = "Company size must be at least 1")
    private Integer companySize;

    @NotNull(message = "Annual revenue is required")
    @Min(value = 0, message = "Annual revenue must be 0 or greater")
    private Double annualRevenue;

    @NotNull(message = "Budget is required")
    @Min(value = 0, message = "Budget must be 0 or greater")
    private Double budget;

    @NotBlank(message = "Timeline is required")
    private String timeline;

    @NotBlank(message = "Requirement details are required")
    private String requirement;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{7,20}$", message = "Please enter a valid phone number")
    private String phoneNumber;

    // Default Constructor
    public LeadAnalysisRequestDto() {
    }

    // Getters and Setters
    public String getLeadName() {
        return leadName;
    }

    public void setLeadName(String leadName) {
        this.leadName = leadName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public Integer getCompanySize() {
        return companySize;
    }

    public void setCompanySize(Integer companySize) {
        this.companySize = companySize;
    }

    public Double getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(Double annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
