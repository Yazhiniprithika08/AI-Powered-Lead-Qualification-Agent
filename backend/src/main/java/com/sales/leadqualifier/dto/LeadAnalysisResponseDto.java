package com.sales.leadqualifier.dto;

/**
 * Data Transfer Object representing the REST API response payload.
 */
public class LeadAnalysisResponseDto {
    private String status;
    private String message;

    // Constructors
    public LeadAnalysisResponseDto() {
    }

    public LeadAnalysisResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
