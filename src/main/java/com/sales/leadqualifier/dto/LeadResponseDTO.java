package com.sales.leadqualifier.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for formatting lead responses returned to the client.
 * Includes demographic, firmographic, and Gemini AI analysis metrics.
 */
public class LeadResponseDTO {
    private String id;
    private String leadName;
    private String companyName;
    private String industry;
    private String jobRole;
    private Integer companySize;
    private Double annualRevenue;
    private Double budget;
    private String timeline;
    private String requirement;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;

    // Phase 3 AI Fields
    private Integer leadScore;
    private String category;
    private String reason;
    private String recommendation;
    private String analysisStatus;
    private LocalDateTime analysisTimestamp;

    // Constructors
    public LeadResponseDTO() {
    }

    public LeadResponseDTO(String id, String leadName, String companyName, String industry, String jobRole, 
                           Integer companySize, Double annualRevenue, Double budget, String timeline, 
                           String requirement, String email, String phoneNumber, LocalDateTime createdAt,
                           Integer leadScore, String category, String reason, String recommendation, 
                           String analysisStatus, LocalDateTime analysisTimestamp) {
        this.id = id;
        this.leadName = leadName;
        this.companyName = companyName;
        this.industry = industry;
        this.jobRole = jobRole;
        this.companySize = companySize;
        this.annualRevenue = annualRevenue;
        this.budget = budget;
        this.timeline = timeline;
        this.requirement = requirement;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.leadScore = leadScore;
        this.category = category;
        this.reason = reason;
        this.recommendation = recommendation;
        this.analysisStatus = analysisStatus;
        this.analysisTimestamp = analysisTimestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getLeadScore() {
        return leadScore;
    }

    public void setLeadScore(Integer leadScore) {
        this.leadScore = leadScore;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public LocalDateTime getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public void setAnalysisTimestamp(LocalDateTime analysisTimestamp) {
        this.analysisTimestamp = analysisTimestamp;
    }
}
