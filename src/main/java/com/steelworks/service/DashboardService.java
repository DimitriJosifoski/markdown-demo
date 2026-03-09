package com.steelworks.service;

import com.steelworks.dto.DashboardSummaryDTO;
import com.steelworks.enums.TimeGrouping;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Orchestration service for the Summary Dashboard ("Meeting Ready" view). AC5: Includes production
 * line rankings. AC6: Includes shipping risk alerts. AC7: Includes defect trends. AC8: Defaults to
 * WEEKLY, supports DAILY and MONTHLY toggle.
 */
@Service
public class DashboardService {

    private final DefectAnalysisService defectAnalysisService;
    private final ShippingStatusService shippingStatusService;

    public DashboardService(DefectAnalysisService defectAnalysisService,
            ShippingStatusService shippingStatusService) {
        this.defectAnalysisService = defectAnalysisService;
        this.shippingStatusService = shippingStatusService;
    }

    /**
     * Builds the complete dashboard summary for the given time grouping. AC8: Defaults to WEEKLY if
     * no time grouping is specified. Aggregates production line rankings (AC5), shipping risk
     * alerts (AC6), and defect trends (AC7).
     *
     * @param timeGrouping
     *            the time grouping for the report (DAILY, WEEKLY, MONTHLY); defaults to WEEKLY if
     *            null
     * @return complete dashboard summary DTO
     */
    public DashboardSummaryDTO getDashboardSummary(TimeGrouping timeGrouping) {
        TimeGrouping effectiveGrouping = timeGrouping == null ? TimeGrouping.WEEKLY : timeGrouping;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (effectiveGrouping) {
            case DAILY -> endDate;
            case WEEKLY -> endDate.minusDays(6);
            case MONTHLY -> endDate.minusDays(29);
        };

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTimeGrouping(effectiveGrouping);
        summary.setProductionLineRankings(
                defectAnalysisService.rankProductionLinesByDefects(startDate, endDate));
        summary.setShippingRiskAlerts(shippingStatusService.getProblematicShippedBatches());
        summary.setDefectTrends(defectAnalysisService.computeDefectTrends(endDate));
        return summary;
    }
}
