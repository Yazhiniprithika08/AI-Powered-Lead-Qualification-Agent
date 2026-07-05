package com.sales.leadqualifier.controller;

import com.sales.leadqualifier.dto.LeadRequestDTO;
import com.sales.leadqualifier.dto.LeadResponseDTO;
import com.sales.leadqualifier.service.LeadQualificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller exposing REST endpoints for managing Leads.
 * Exposes CRUD, search, sorting, and AI retry analysis endpoints.
 */
@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private static final Logger logger = LoggerFactory.getLogger(LeadController.class);

    private final LeadQualificationService leadQualificationService;

    @Autowired
    public LeadController(LeadQualificationService leadQualificationService) {
        this.leadQualificationService = leadQualificationService;
    }

    /**
     * Endpoint to save and qualify a new lead.
     * Maps the incoming details, saves to MongoDB, triggers Gemini AI analysis,
     * updates the same document, and returns AI scoring details.
     *
     * @param requestDto lead profile details
     * @return payload containing AI analysis metrics and ID of the created document
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeLead(@Valid @RequestBody LeadRequestDTO requestDto) {
        logger.info("Received request to qualify lead: Name='{}', Company='{}'", 
                    requestDto.getLeadName(), requestDto.getCompanyName());
        
        LeadResponseDTO savedLead = leadQualificationService.saveLead(requestDto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("leadId", savedLead.getId());
        response.put("leadScore", savedLead.getLeadScore());
        response.put("category", savedLead.getCategory());
        response.put("reason", savedLead.getReason());
        response.put("recommendation", savedLead.getRecommendation());
        response.put("analysisStatus", savedLead.getAnalysisStatus());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve all lead documents stored in database.
     * Supports sorting by date ("newest" or "oldest").
     *
     * @param sort sorting order (default: "newest")
     * @return list of lead DTOs
     */
    @GetMapping
    public ResponseEntity<List<LeadResponseDTO>> getAllLeads(
            @RequestParam(value = "sort", defaultValue = "newest") String sort) {
        logger.info("REST request to fetch all leads sorted by: {}", sort);
        List<LeadResponseDTO> leads = leadQualificationService.getAllLeads(sort);
        return ResponseEntity.ok(leads);
    }

    /**
     * Retrieve details of a single lead.
     *
     * @param id MongoDB identifier
     * @return matching lead details
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeadResponseDTO> getLeadById(@PathVariable String id) {
        logger.info("REST request to fetch details for lead ID: {}", id);
        LeadResponseDTO lead = leadQualificationService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    /**
     * Update details of an existing lead. Re-runs Gemini AI analysis on the lead.
     *
     * @param id MongoDB identifier
     * @param requestDto updated attributes
     * @return updated lead details
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeadResponseDTO> updateLead(
            @PathVariable String id, 
            @Valid @RequestBody LeadRequestDTO requestDto) {
        logger.info("REST request to update lead ID: {}", id);
        LeadResponseDTO updatedLead = leadQualificationService.updateLead(id, requestDto);
        return ResponseEntity.ok(updatedLead);
    }

    /**
     * Delete a lead document.
     *
     * @param id MongoDB identifier
     * @return confirmation status payload
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLead(@PathVariable String id) {
        logger.info("REST request to delete lead ID: {}", id);
        leadQualificationService.deleteLead(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Lead deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search lead documents by company name.
     *
     * @param company company name snippet
     * @return list of matching leads
     */
    @GetMapping("/search")
    public ResponseEntity<List<LeadResponseDTO>> searchLeadsByCompany(@RequestParam("company") String company) {
        logger.info("REST request to search leads for company snippet: '{}'", company);
        List<LeadResponseDTO> leads = leadQualificationService.searchLeadByCompany(company);
        return ResponseEntity.ok(leads);
    }

    /**
     * Retry B2B sales analysis using Gemini AI on an existing lead record.
     * Updates the same MongoDB document.
     *
     * @param id MongoDB identifier
     * @return updated lead response
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<LeadResponseDTO> retryAnalysis(@PathVariable String id) {
        logger.info("REST request to retry analysis for lead ID: {}", id);
        LeadResponseDTO lead = leadQualificationService.retryAnalysis(id);
        return ResponseEntity.ok(lead);
    }
}
