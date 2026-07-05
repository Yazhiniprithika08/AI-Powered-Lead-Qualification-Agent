package com.sales.leadqualifier.controller;

import com.sales.leadqualifier.dto.DashboardSummaryDTO;
import com.sales.leadqualifier.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller exposing REST endpoints for analytics and dashboard reports.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Endpoint to fetch general KPI details.
     *
     * @return REST Response carrying dashboard numbers
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        logger.info("REST request to fetch dashboard summaries.");
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Endpoint to fetch category distributions (Hot, Warm, Cold).
     *
     * @return REST Response carrying category distributions map
     */
    @GetMapping("/category-distribution")
    public ResponseEntity<Map<String, Long>> getCategoryDistribution() {
        logger.info("REST request to fetch lead category distributions.");
        Map<String, Long> distribution = dashboardService.getCategoryDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * Endpoint to fetch monthly lead counts chronologically.
     *
     * @return REST Response carrying monthly lead stats map
     */
    @GetMapping("/monthly-analysis")
    public ResponseEntity<Map<String, Long>> getMonthlyAnalysis() {
        logger.info("REST request to fetch monthly analysis stats.");
        Map<String, Long> monthlyStats = dashboardService.getMonthlyAnalysis();
        return ResponseEntity.ok(monthlyStats);
    }

    /**
     * Endpoint to fetch monthly average lead scores chronologically.
     *
     * @return REST Response carrying monthly average scores map
     */
    @GetMapping("/monthly-score-trend")
    public ResponseEntity<Map<String, Double>> getMonthlyScoreTrend() {
        logger.info("REST request to fetch monthly score trend stats.");
        Map<String, Double> scoreTrend = dashboardService.getMonthlyScoreTrend();
        return ResponseEntity.ok(scoreTrend);
    }
}
