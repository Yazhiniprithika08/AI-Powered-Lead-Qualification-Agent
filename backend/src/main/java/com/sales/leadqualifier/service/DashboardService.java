package com.sales.leadqualifier.service;

import com.sales.leadqualifier.dto.DashboardSummaryDTO;
import java.util.Map;

/**
 * Service interface defining business logic for dashboard analytics.
 */
public interface DashboardService {

    /**
     * Compute KPI statistics from the database.
     *
     * @return summary dashboard numbers
     */
    DashboardSummaryDTO getDashboardSummary();

    /**
     * Group count leads by category (Hot, Warm, Cold).
     *
     * @return map mapping category name to lead count
     */
    Map<String, Long> getCategoryDistribution();

    /**
     * Compute chronological lead generation counts grouped by month (e.g. "Jul 2026").
     *
     * @return map of month labels to counts
     */
    Map<String, Long> getMonthlyAnalysis();

    /**
     * Compute chronological average lead scores grouped by month (e.g. "Jul 2026").
     *
     * @return map of month labels to average score values
     */
    Map<String, Double> getMonthlyScoreTrend();
}
