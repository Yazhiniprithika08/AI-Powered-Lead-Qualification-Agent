package com.sales.leadqualifier.service;

import com.sales.leadqualifier.dto.LeadRequestDTO;
import com.sales.leadqualifier.dto.LeadResponseDTO;
import java.util.List;

/**
 * Service interface defining business operations for lead management and AI analysis.
 */
public interface LeadQualificationService {
    
    /**
     * Save lead details into MongoDB and trigger Google Gemini AI analysis.
     *
     * @param request lead details to validate and save
     * @return saved lead response containing ID, timestamp, and AI metrics
     */
    LeadResponseDTO saveLead(LeadRequestDTO request);

    /**
     * Retrieve all saved leads, sorted by creation date.
     *
     * @param sortBy sort order directive ("newest" or "oldest")
     * @return list of lead responses
     */
    List<LeadResponseDTO> getAllLeads(String sortBy);

    /**
     * Retrieve details of a single lead by its unique database ID.
     *
     * @param id MongoDB identifier
     * @return matching lead details DTO
     */
    LeadResponseDTO getLeadById(String id);

    /**
     * Update fields of an existing lead record. Re-triggers Gemini analysis.
     *
     * @param id MongoDB identifier
     * @param request updated attributes
     * @return updated lead details DTO
     */
    LeadResponseDTO updateLead(String id, LeadRequestDTO request);

    /**
     * Delete a lead record from MongoDB.
     *
     * @param id MongoDB identifier
     */
    void deleteLead(String id);

    /**
     * Search lead records by company name snippet.
     *
     * @param companyName company name snippet (case-insensitive query)
     * @return list of matching leads
     */
    List<LeadResponseDTO> searchLeadByCompany(String companyName);

    /**
     * Retry Google Gemini B2B evaluation on an existing lead record.
     * Updates the same MongoDB document without duplicates.
     *
     * @param id MongoDB identifier
     * @return updated lead response containing AI results
     */
    /**
     * Retry Google Gemini B2B evaluation on an existing lead record.
     * Updates the same MongoDB document without duplicates.
     *
     * @param id MongoDB identifier
     * @return updated lead response containing AI results
     */
    LeadResponseDTO retryAnalysis(String id);

    /**
     * Test the connection to the Google Gemini API with a mock lead.
     *
     * @return Gemini analysis result for verification
     */
    com.sales.leadqualifier.dto.GeminiAnalysisResultDTO testGeminiConnection();
}
