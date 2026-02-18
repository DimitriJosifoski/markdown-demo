package com.steelworks.dto;

import com.steelworks.enums.TimeGrouping;

import java.util.List;

/**
 * Top-level DTO for the summary dashboard ("Meeting Ready" view).
 * AC5: Contains production line rankings.
 * AC6: Contains shipping risk alerts.
 * AC7: Contains defect trends.
 * AC8: Reflects the selected time grouping (defaults to WEEKLY).
 */
public class DashboardSummaryDTO {

    private TimeGrouping timeGrouping;
    private List<ProductionLineRankingDTO> productionLineRankings;
    private List<ShippingRiskAlertDTO> shippingRiskAlerts;
    private List<DefectTrendDTO> defectTrends;

    public DashboardSummaryDTO() {}

    public TimeGrouping getTimeGrouping() { return timeGrouping; }
    public void setTimeGrouping(TimeGrouping timeGrouping) { this.timeGrouping = timeGrouping; }

    public List<ProductionLineRankingDTO> getProductionLineRankings() { return productionLineRankings; }
    public void setProductionLineRankings(List<ProductionLineRankingDTO> productionLineRankings) { this.productionLineRankings = productionLineRankings; }

    public List<ShippingRiskAlertDTO> getShippingRiskAlerts() { return shippingRiskAlerts; }
    public void setShippingRiskAlerts(List<ShippingRiskAlertDTO> shippingRiskAlerts) { this.shippingRiskAlerts = shippingRiskAlerts; }

    public List<DefectTrendDTO> getDefectTrends() { return defectTrends; }
    public void setDefectTrends(List<DefectTrendDTO> defectTrends) { this.defectTrends = defectTrends; }
}
