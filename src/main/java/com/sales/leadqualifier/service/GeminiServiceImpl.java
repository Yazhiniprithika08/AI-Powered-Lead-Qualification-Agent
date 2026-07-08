package com.sales.leadqualifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.leadqualifier.dto.GeminiAnalysisResultDTO;
import com.sales.leadqualifier.dto.GeminiRequestDTO;
import com.sales.leadqualifier.dto.GeminiResponseDTO;
import com.sales.leadqualifier.model.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service implementation for interacting with the Google Gemini API.
 * Assembles lead parameters, runs prompt engineering rules, handles network calls,
 * and parses JSON response payloads.
 */
@Service
public class GeminiServiceImpl implements GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiServiceImpl.class);

    private static final String GEMINI_API_URL = 
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    public GeminiServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public GeminiAnalysisResultDTO analyzeLead(Lead lead) {
        logger.info("Triggering Gemini AI analysis for Lead: '{}' from Company: '{}'", 
                    lead.getLeadName(), lead.getCompanyName());

        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_GEMINI_API_KEY_HERE".equals(apiKey)) {
            throw new IllegalArgumentException("Invalid Google Gemini API key configured in application.properties.");
        }

        // 1. Build prompt template based on B2B scoring instructions
        String prompt = buildPrompt(lead);
        logger.debug("Engineered Prompt: {}", prompt);

        // 2. Build Request DTO
        GeminiRequestDTO requestDto = new GeminiRequestDTO(prompt);

        try {
            // 3. POST request to Gemini endpoint
            String targetUrl = GEMINI_API_URL + apiKey.trim();
            GeminiResponseDTO responseDto = restTemplate.postForObject(targetUrl, requestDto, GeminiResponseDTO.class);

            if (responseDto == null || responseDto.getCandidates() == null || responseDto.getCandidates().isEmpty()) {
                throw new RuntimeException("Empty response received from Gemini API.");
            }

            // 4. Extract Text response content
            String rawJsonText = responseDto.getCandidates().get(0).getContent().getParts().get(0).getText();
            logger.debug("Raw Gemini Text response: {}", rawJsonText);

            // Sanitize raw text to strip out markdown JSON fence blocks if present
            String sanitizedJson = sanitizeJsonText(rawJsonText);
            logger.debug("Sanitized JSON payload: {}", sanitizedJson);

            // 5. Parse JSON string into DTO using Jackson ObjectMapper
            GeminiAnalysisResultDTO result = objectMapper.readValue(sanitizedJson, GeminiAnalysisResultDTO.class);
            
            logger.info("Successfully parsed Gemini response. Score: {}, Category: {}", 
                        result.getLeadScore(), result.getCategory());
            
            return result;

        } catch (Exception ex) {
            logger.error("Error occurred during Google Gemini API call / parsing:", ex);
            throw new RuntimeException("Gemini AI API processing failure: " + ex.getMessage(), ex);
        }
    }

    /**
     * Constructs B2B sales evaluator prompt matching exact rules.
     */
    private String buildPrompt(Lead lead) {
        return "You are an experienced B2B Sales Manager.\n" +
                "Evaluate this business lead using the following criteria:\n" +
                "- Lead Name: " + lead.getLeadName() + "\n" +
                "- Company Name: " + lead.getCompanyName() + "\n" +
                "- Industry: " + lead.getIndustry() + "\n" +
                "- Job Role: " + lead.getJobRole() + "\n" +
                "- Company Size (Employees): " + lead.getCompanySize() + "\n" +
                "- Annual Revenue (USD): " + lead.getAnnualRevenue() + "\n" +
                "- Budget (USD): " + lead.getBudget() + "\n" +
                "- Timeline: " + lead.getTimeline() + "\n" +
                "- Business Requirement: " + lead.getRequirement() + "\n" +
                "- Email: " + lead.getEmail() + "\n" +
                "- Phone Number: " + lead.getPhoneNumber() + "\n\n" +
                "Scoring Rules:\n" +
                "1. Company Size (20 Marks):\n" +
                "   - Enterprise (1000+ employees) = 20\n" +
                "   - Medium (250-999 employees) = 15\n" +
                "   - Small (50-249 employees) = 10\n" +
                "   - Startup (<50 employees) = 5\n" +
                "   (Determine B2B category based on the lead's company size of " + lead.getCompanySize() + " employees)\n\n" +
                "2. Budget (25 Marks):\n" +
                "   - Above ₹20,00,000 = 25\n" +
                "   - ₹10,00,000–₹20,00,000 = 20\n" +
                "   - ₹5,00,000–₹10,00,000 = 15\n" +
                "   - Below ₹5,00,000 = 5\n" +
                "   (IMPORTANT: Convert the lead's budget of " + lead.getBudget() + " USD into Indian Rupees (INR) at an approximate conversion rate of 1 USD = 83 INR, yielding approx ₹" + Math.round(lead.getBudget() * 83) + ", and apply the rule accordingly)\n\n" +
                "3. Timeline (15 Marks):\n" +
                "   - Immediate / Immediate (Within 30 Days) = 15\n" +
                "   - Within 1 Month / 1 to 3 Months = 12\n" +
                "   - Within 3 Months / 3 to 6 Months = 8\n" +
                "   - More than 3 Months / 6+ Months = 3\n" +
                "   (timeline is '" + lead.getTimeline() + "')\n\n" +
                "4. Business Requirement (20 Marks):\n" +
                "   - Clear = 20\n" +
                "   - Moderate = 10\n" +
                "   - Unclear = 5\n" +
                "   (Assess the description: '" + lead.getRequirement() + "')\n\n" +
                "5. Job Role (20 Marks):\n" +
                "   - CEO / CTO / Director / VP / Chief = 20\n" +
                "   - Manager = 15\n" +
                "   - Team Lead = 10\n" +
                "   - Employee = 5\n" +
                "   (Job role is '" + lead.getJobRole() + "')\n\n" +
                "Total Score = 100\n\n" +
                "After calculating the total score, classify the lead:\n" +
                "- 80–100 = Hot Lead\n" +
                "- 50–79 = Warm Lead\n" +
                "- 0–49 = Cold Lead\n\n" +
                "Return ONLY a valid JSON block matching this schema:\n" +
                "{\n" +
                "  \"leadScore\": 0,\n" +
                "  \"category\": \"\",\n" +
                "  \"reason\": \"\",\n" +
                "  \"recommendation\": \"\"\n" +
                "}\n\n" +
                "Do NOT return markdown blocks. Do NOT return explanations outside of the JSON block.";
    }

    /**
     * Sanitizes raw response text by trimming spaces and stripping markdown JSON code blocks.
     */
    private String sanitizeJsonText(String rawText) {
        if (rawText == null) return "{}";
        String trimmed = rawText.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
