package com.sales.leadqualifier.service;

import com.sales.leadqualifier.dto.GeminiAnalysisResultDTO;
import com.sales.leadqualifier.model.Lead;

/**
 * Service interface defining Gemini AI analysis operations.
 */
public interface GeminiService {

    /**
     * Query Google Gemini to analyze and evaluate lead firmographics.
     *
     * @param lead domain entity containing lead inputs
     * @return parsed B2B scoring details DTO
     */
    GeminiAnalysisResultDTO analyzeLead(Lead lead);
}
