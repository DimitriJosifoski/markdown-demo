package com.steelworks.dto;

import java.time.LocalDate;

/**
 * Input DTO for lot lookup requests. Supports searching by lot ID (with fuzzy matching) and/or date
 * range.
 */
public class LotSearchRequest {

    private String lotId;
    private LocalDate startDate;
    private LocalDate endDate;

    public LotSearchRequest() {
    }

    public String getLotId() {
        return lotId;
    }
    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
