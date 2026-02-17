package com.steelworks.tracker.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DashboardDTO — Top-level DTO that aggregates all dashboard data for the
 * "Meeting Ready" summary view (AC5–AC8).
 *
 * <p>The {@link com.steelworks.tracker.controller.DashboardController} passes
 * a single instance of this DTO to the Thymeleaf template so it can render
 * every section of the dashboard in one page load.</p>
 *
 * @param lineRankings        production lines ranked by defect count (AC5)
 * @param shippingRisks       lots with critical defects that shipped (AC6)
 * @param defectTrends        defect types with up/down trend indicators (AC7)
 * @param orphanedLots        lots with no production/shipping data (AC10)
 * @param dataConflicts       lots appearing on multiple production lines (AC11)
 * @param periodStart         start date of the reporting period
 * @param periodEnd           end date of the reporting period
 * @param timeGrouping        current grouping: "WEEKLY" (default), "DAILY", or "MONTHLY" (AC8)
 */
public record DashboardDTO(
        List<LineDefectCountDTO> lineRankings,
        List<ShippingRiskDTO> shippingRisks,
        List<DefectTrendDTO> defectTrends,
        List<OrphanedLotDTO> orphanedLots,
        List<DataConflictDTO> dataConflicts,
        LocalDate periodStart,
        LocalDate periodEnd,
        String timeGrouping
) {
}
