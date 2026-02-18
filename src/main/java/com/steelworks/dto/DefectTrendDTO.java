package com.steelworks.dto;

/**
 * DTO for defect trend analysis.
 * AC7: Shows if a defect type has increased or decreased in frequency
 *      compared to the previous 7-day period.
 */
public class DefectTrendDTO {

    private String defectName;
    private long currentPeriodCount;
    private long previousPeriodCount;
    private TrendDirection trendDirection;

    /**
     * Visual indicator for defect trend direction.
     * AC7: Represents up/down arrow for frequency change.
     */
    public enum TrendDirection {
        INCREASING,
        DECREASING,
        STABLE
    }

    public DefectTrendDTO() {}

    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }

    public long getCurrentPeriodCount() { return currentPeriodCount; }
    public void setCurrentPeriodCount(long currentPeriodCount) { this.currentPeriodCount = currentPeriodCount; }

    public long getPreviousPeriodCount() { return previousPeriodCount; }
    public void setPreviousPeriodCount(long previousPeriodCount) { this.previousPeriodCount = previousPeriodCount; }

    public TrendDirection getTrendDirection() { return trendDirection; }
    public void setTrendDirection(TrendDirection trendDirection) { this.trendDirection = trendDirection; }
}
