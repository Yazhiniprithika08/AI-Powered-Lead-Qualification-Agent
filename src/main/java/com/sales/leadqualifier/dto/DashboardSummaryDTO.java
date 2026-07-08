package com.sales.leadqualifier.dto;

/**
 * Data Transfer Object containing consolidated metrics for the dashboard KPIs.
 */
public class DashboardSummaryDTO {
    private long totalLeads;
    private long hotLeads;
    private long warmLeads;
    private long coldLeads;
    private long pendingAnalysis;
    private long failedAnalysis;
    private int averageLeadScore;
    private String successRate;

    // Constructors
    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(long totalLeads, long hotLeads, long warmLeads, long coldLeads, 
                               long pendingAnalysis, long failedAnalysis, int averageLeadScore, String successRate) {
        this.totalLeads = totalLeads;
        this.hotLeads = hotLeads;
        this.warmLeads = warmLeads;
        this.coldLeads = coldLeads;
        this.pendingAnalysis = pendingAnalysis;
        this.failedAnalysis = failedAnalysis;
        this.averageLeadScore = averageLeadScore;
        this.successRate = successRate;
    }

    // Getters and Setters
    public long getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(long totalLeads) {
        this.totalLeads = totalLeads;
    }

    public long getHotLeads() {
        return hotLeads;
    }

    public void setHotLeads(long hotLeads) {
        this.hotLeads = hotLeads;
    }

    public long getWarmLeads() {
        return warmLeads;
    }

    public void setWarmLeads(long warmLeads) {
        this.warmLeads = warmLeads;
    }

    public long getColdLeads() {
        return coldLeads;
    }

    public void setColdLeads(long coldLeads) {
        this.coldLeads = coldLeads;
    }

    public long getPendingAnalysis() {
        return pendingAnalysis;
    }

    public void setPendingAnalysis(long pendingAnalysis) {
        this.pendingAnalysis = pendingAnalysis;
    }

    public long getFailedAnalysis() {
        return failedAnalysis;
    }

    public void setFailedAnalysis(long failedAnalysis) {
        this.failedAnalysis = failedAnalysis;
    }

    public int getAverageLeadScore() {
        return averageLeadScore;
    }

    public void setAverageLeadScore(int averageLeadScore) {
        this.averageLeadScore = averageLeadScore;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }
}
