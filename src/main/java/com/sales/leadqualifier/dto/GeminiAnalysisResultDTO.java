package com.sales.leadqualifier.dto;

/**
 * Data Transfer Object representing the structured B2B JSON output returned inside Gemini's text block.
 */
public class GeminiAnalysisResultDTO {
    private Integer leadScore;
    private String category;
    private String reason;
    private String recommendation;

    public GeminiAnalysisResultDTO() {
    }

    // Getters and Setters
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
}
