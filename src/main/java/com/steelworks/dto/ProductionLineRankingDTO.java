package com.steelworks.dto;

/**
 * DTO for production line defect ranking. AC5: Ranks production lines by total defect count for the
 * current period.
 */
public class ProductionLineRankingDTO {

    private String lineName;
    private long totalDefects;
    private int rank;

    public ProductionLineRankingDTO() {
    }

    public String getLineName() {
        return lineName;
    }
    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public long getTotalDefects() {
        return totalDefects;
    }
    public void setTotalDefects(long totalDefects) {
        this.totalDefects = totalDefects;
    }

    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }
}
