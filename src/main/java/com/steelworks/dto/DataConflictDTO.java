package com.steelworks.dto;

import java.util.List;

/**
 * DTO for data conflict alerts. AC11: If the same Lot ID is associated with two different
 * Production Lines across files, flags a "Data Conflict" for manual review.
 */
public class DataConflictDTO {

    private String lotIdentifier;
    private List<String> conflictingProductionLines;
    private String description;

    public DataConflictDTO() {
    }

    public String getLotIdentifier() {
        return lotIdentifier;
    }
    public void setLotIdentifier(String lotIdentifier) {
        this.lotIdentifier = lotIdentifier;
    }

    public List<String> getConflictingProductionLines() {
        return conflictingProductionLines;
    }
    public void setConflictingProductionLines(List<String> conflictingProductionLines) {
        this.conflictingProductionLines = conflictingProductionLines;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
