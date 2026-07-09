package com.sales.leadqualifier.service;

import com.sales.leadqualifier.dto.GeminiAnalysisResultDTO;
import com.sales.leadqualifier.dto.LeadRequestDTO;
import com.sales.leadqualifier.dto.LeadResponseDTO;
import com.sales.leadqualifier.exception.LeadNotFoundException;
import com.sales.leadqualifier.model.Lead;
import com.sales.leadqualifier.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation managing operations on Lead entities.
 * Handles database interaction via LeadRepository and triggers Gemini AI analysis.
 */
@Service
public class LeadQualificationServiceImpl implements LeadQualificationService {

    private static final Logger logger = LoggerFactory.getLogger(LeadQualificationServiceImpl.class);

    private final LeadRepository leadRepository;
    private final GeminiService geminiService;

    @Autowired
    public LeadQualificationServiceImpl(LeadRepository leadRepository, GeminiService geminiService) {
        this.leadRepository = leadRepository;
        this.geminiService = geminiService;
    }

    @Override
    public LeadResponseDTO saveLead(LeadRequestDTO request) {
        logger.info("Saving new lead and initiating analysis: Name='{}', Company='{}'", 
                    request.getLeadName(), request.getCompanyName());
        
        // 1. Initial save of demographic/firmographic details with PENDING status
        Lead lead = new Lead(
                request.getLeadName(),
                request.getCompanyName(),
                request.getIndustry(),
                request.getJobRole(),
                request.getCompanySize(),
                request.getAnnualRevenue(),
                request.getBudget(),
                request.getTimeline(),
                request.getRequirement(),
                request.getEmail(),
                request.getPhoneNumber()
        );
        lead.setCreatedAt(LocalDateTime.now());
        lead.setAnalysisStatus("PENDING");
        
        Lead savedLead = leadRepository.save(lead);
        logger.debug("Lead saved with Mongo ID: {}. Triggering AI analysis...", savedLead.getId());
        
        // 2. Perform Google Gemini AI Analysis
        try {
            GeminiAnalysisResultDTO aiResult = geminiService.analyzeLead(savedLead);
            savedLead.setLeadScore(aiResult.getLeadScore());
            savedLead.setCategory(aiResult.getCategory());
            savedLead.setReason(aiResult.getReason());
            savedLead.setRecommendation(aiResult.getRecommendation());
            savedLead.setAnalysisStatus("SUCCESS");
            savedLead.setAnalysisTimestamp(LocalDateTime.now());
        } catch (Exception ex) {
            logger.error("AI Analysis failed for lead ID: " + savedLead.getId() + ". Setting status to FAILED.", ex);
            savedLead.setAnalysisStatus("FAILED");
            savedLead.setAnalysisTimestamp(LocalDateTime.now());
        }
        
        // 3. Save the updated lead document (Updates the SAME document, no duplicates)
        Lead finalLead = leadRepository.save(savedLead);
        return convertToResponseDTO(finalLead);
    }

    @Override
    public List<LeadResponseDTO> getAllLeads(String sortBy) {
        logger.info("Retrieving all leads sorted by: {}", sortBy);
        
        Sort.Direction direction = Sort.Direction.DESC; // Default to newest first
        if ("oldest".equalsIgnoreCase(sortBy)) {
            direction = Sort.Direction.ASC;
        }
        
        Sort sort = Sort.by(direction, "createdAt");
        
        return leadRepository.findAll(sort).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeadResponseDTO getLeadById(String id) {
        logger.info("Retrieving lead with ID: {}", id);
        
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new LeadNotFoundException(id));
        
        return convertToResponseDTO(lead);
    }

    @Override
    public LeadResponseDTO updateLead(String id, LeadRequestDTO request) {
        logger.info("Updating lead and re-running analysis: ID={}", id);
        
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new LeadNotFoundException(id));
        
        // Update core fields
        lead.setLeadName(request.getLeadName());
        lead.setCompanyName(request.getCompanyName());
        lead.setIndustry(request.getIndustry());
        lead.setJobRole(request.getJobRole());
        lead.setCompanySize(request.getCompanySize());
        lead.setAnnualRevenue(request.getAnnualRevenue());
        lead.setBudget(request.getBudget());
        lead.setTimeline(request.getTimeline());
        lead.setRequirement(request.getRequirement());
        lead.setEmail(request.getEmail());
        lead.setPhoneNumber(request.getPhoneNumber());
        
        // Reset status for re-analysis
        lead.setAnalysisStatus("PENDING");
        Lead saved = leadRepository.save(lead);
        
        // Trigger AI analysis again
        try {
            GeminiAnalysisResultDTO aiResult = geminiService.analyzeLead(saved);
            saved.setLeadScore(aiResult.getLeadScore());
            saved.setCategory(aiResult.getCategory());
            saved.setReason(aiResult.getReason());
            saved.setRecommendation(aiResult.getRecommendation());
            saved.setAnalysisStatus("SUCCESS");
            saved.setAnalysisTimestamp(LocalDateTime.now());
        } catch (Exception ex) {
            logger.error("AI Analysis failed on update for lead ID: " + id + ". Setting status to FAILED.", ex);
            saved.setAnalysisStatus("FAILED");
            saved.setAnalysisTimestamp(LocalDateTime.now());
        }
        
        Lead updatedLead = leadRepository.save(saved);
        return convertToResponseDTO(updatedLead);
    }

    @Override
    public void deleteLead(String id) {
        logger.info("Deleting lead with ID: {}", id);
        
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new LeadNotFoundException(id));
        
        leadRepository.delete(lead);
        logger.debug("Lead deleted successfully: {}", id);
    }

    @Override
    public List<LeadResponseDTO> searchLeadByCompany(String companyName) {
        logger.info("Searching leads by company matching: '{}'", companyName);
        
        return leadRepository.findByCompanyNameContainingIgnoreCase(companyName).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeadResponseDTO retryAnalysis(String id) {
        logger.info("Retrying AI analysis for lead ID: {}", id);
        
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new LeadNotFoundException(id));
        
        // Set PENDING and save in case UI checks status
        lead.setAnalysisStatus("PENDING");
        leadRepository.save(lead);
        
        try {
            GeminiAnalysisResultDTO aiResult = geminiService.analyzeLead(lead);
            lead.setLeadScore(aiResult.getLeadScore());
            lead.setCategory(aiResult.getCategory());
            lead.setReason(aiResult.getReason());
            lead.setRecommendation(aiResult.getRecommendation());
            lead.setAnalysisStatus("SUCCESS");
            lead.setAnalysisTimestamp(LocalDateTime.now());
        } catch (Exception ex) {
            logger.error("Retry AI Analysis failed for lead ID: " + id + ". Setting status to FAILED.", ex);
            lead.setAnalysisStatus("FAILED");
            lead.setAnalysisTimestamp(LocalDateTime.now());
        }
        
        Lead finalLead = leadRepository.save(lead);
        return convertToResponseDTO(finalLead);
    }

    @Override
    public com.sales.leadqualifier.dto.GeminiAnalysisResultDTO testGeminiConnection() {
        logger.info("Executing diagnostic test of Google Gemini API connection");
        Lead testLead = new Lead();
        testLead.setLeadName("Test User");
        testLead.setCompanyName("Test Corp");
        testLead.setIndustry("Technology");
        testLead.setJobRole("CEO");
        testLead.setCompanySize(1500);
        testLead.setAnnualRevenue(50000000.0);
        testLead.setBudget(500000.0);
        testLead.setTimeline("Immediate");
        testLead.setRequirement("Enterprise cloud migration services");
        testLead.setEmail("test@testcorp.com");
        testLead.setPhoneNumber("1234567890");
        return geminiService.analyzeLead(testLead);
    }

    /**
     * Helper to map Lead entity to LeadResponseDTO.
     */
    private LeadResponseDTO convertToResponseDTO(Lead lead) {
        return new LeadResponseDTO(
                lead.getId(),
                lead.getLeadName(),
                lead.getCompanyName(),
                lead.getIndustry(),
                lead.getJobRole(),
                lead.getCompanySize(),
                lead.getAnnualRevenue(),
                lead.getBudget(),
                lead.getTimeline(),
                lead.getRequirement(),
                lead.getEmail(),
                lead.getPhoneNumber(),
                lead.getCreatedAt(),
                lead.getLeadScore(),
                lead.getCategory(),
                lead.getReason(),
                lead.getRecommendation(),
                lead.getAnalysisStatus(),
                lead.getAnalysisTimestamp()
        );
    }
}
