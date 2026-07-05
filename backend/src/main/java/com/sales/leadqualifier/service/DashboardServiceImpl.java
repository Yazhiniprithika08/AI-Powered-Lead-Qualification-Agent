package com.sales.leadqualifier.service;

import com.sales.leadqualifier.dto.DashboardSummaryDTO;
import com.sales.leadqualifier.model.Lead;
import com.sales.leadqualifier.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation aggregating database values to return dashboard metrics.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final LeadRepository leadRepository;

    @Autowired
    public DashboardServiceImpl(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    public DashboardSummaryDTO getDashboardSummary() {
        logger.info("Computing dashboard summary KPI metrics.");
        List<Lead> leads = leadRepository.findAll();

        if (leads.isEmpty()) {
            return new DashboardSummaryDTO(0, 0, 0, 0, 0, 0, 0, "0%");
        }

        long totalLeads = leads.size();
        long hotLeads = 0;
        long warmLeads = 0;
        long coldLeads = 0;
        long pendingAnalysis = 0;
        long failedAnalysis = 0;
        long successLeads = 0;

        long scoreSum = 0;
        long scoreCount = 0;

        for (Lead l : leads) {
            // Count categories
            String cat = l.getCategory();
            if (cat != null) {
                String lowercaseCat = cat.toLowerCase();
                if (lowercaseCat.contains("hot")) hotLeads++;
                else if (lowercaseCat.contains("warm")) warmLeads++;
                else if (lowercaseCat.contains("cold")) coldLeads++;
            }

            // Count status
            String status = l.getAnalysisStatus();
            if ("PENDING".equalsIgnoreCase(status)) {
                pendingAnalysis++;
            } else if ("FAILED".equalsIgnoreCase(status)) {
                failedAnalysis++;
            } else if ("SUCCESS".equalsIgnoreCase(status)) {
                successLeads++;
            }

            // Calculate score sum
            if (l.getLeadScore() != null) {
                scoreSum += l.getLeadScore();
                scoreCount++;
            }
        }

        int averageLeadScore = scoreCount > 0 ? (int) Math.round((double) scoreSum / scoreCount) : 0;
        
        // Calculate success rate: Success Status / Total Leads
        int successPercent = (int) Math.round(((double) successLeads / totalLeads) * 100);
        String successRate = successPercent + "%";

        return new DashboardSummaryDTO(
                totalLeads,
                hotLeads,
                warmLeads,
                coldLeads,
                pendingAnalysis,
                failedAnalysis,
                averageLeadScore,
                successRate
        );
    }

    @Override
    public Map<String, Long> getCategoryDistribution() {
        logger.info("Computing lead category distributions.");
        List<Lead> leads = leadRepository.findAll();

        long hot = 0;
        long warm = 0;
        long cold = 0;

        for (Lead l : leads) {
            String cat = l.getCategory();
            if (cat != null) {
                String lowercaseCat = cat.toLowerCase();
                if (lowercaseCat.contains("hot")) hot++;
                else if (lowercaseCat.contains("warm")) warm++;
                else if (lowercaseCat.contains("cold")) cold++;
            }
        }

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("Hot", hot);
        distribution.put("Warm", warm);
        distribution.put("Cold", cold);

        return distribution;
    }

    @Override
    public Map<String, Long> getMonthlyAnalysis() {
        logger.info("Computing monthly lead creation statistics.");
        List<Lead> leads = leadRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

        // Group by YearMonth to ensure chronological sorting
        Map<YearMonth, Long> grouped = leads.stream()
                .filter(l -> l.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        l -> YearMonth.from(l.getCreatedAt()),
                        TreeMap::new, // Keep keys sorted chronologically
                        Collectors.counting()
                ));

        Map<String, Long> monthlyCounts = new LinkedHashMap<>();
        grouped.forEach((yearMonth, count) -> {
            monthlyCounts.put(yearMonth.format(formatter), count);
        });

        // If no records exist, return a dummy placeholder for UI consistency
        if (monthlyCounts.isEmpty()) {
            YearMonth current = YearMonth.now();
            monthlyCounts.put(current.format(formatter), 0L);
        }

        return monthlyCounts;
    }

    @Override
    public Map<String, Double> getMonthlyScoreTrend() {
        logger.info("Computing monthly average lead score statistics.");
        List<Lead> leads = leadRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

        // Group by YearMonth to ensure chronological sorting, filter leads that have a score
        Map<YearMonth, List<Lead>> grouped = leads.stream()
                .filter(l -> l.getCreatedAt() != null && l.getLeadScore() != null)
                .collect(Collectors.groupingBy(
                        l -> YearMonth.from(l.getCreatedAt()),
                        TreeMap::new,
                        Collectors.toList()
                ));

        Map<String, Double> monthlyScores = new LinkedHashMap<>();
        grouped.forEach((yearMonth, monthLeads) -> {
            double avgScore = monthLeads.stream()
                    .mapToDouble(Lead::getLeadScore)
                    .average()
                    .orElse(0.0);
            
            // Round to 1 decimal place
            double roundedAvg = Math.round(avgScore * 10.0) / 10.0;
            monthlyScores.put(yearMonth.format(formatter), roundedAvg);
        });

        // If no records exist, return a dummy placeholder for UI consistency
        if (monthlyScores.isEmpty()) {
            YearMonth current = YearMonth.now();
            monthlyScores.put(current.format(formatter), 0.0);
        }

        return monthlyScores;
    }
}
