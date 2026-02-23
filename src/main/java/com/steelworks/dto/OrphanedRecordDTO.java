package com.steelworks.dto;

/**
 * DTO for orphaned records. AC10: If a Lot ID exists in Quality but not in Production or Shipping,
 * it is flagged as "Orphaned Data" rather than excluded.
 */
public class OrphanedRecordDTO {

    private String lotIdentifier;
    private boolean inProduction;
    private boolean inShipping;
    private boolean inQuality;
    private String reason;

    public OrphanedRecordDTO() {
    }

    public String getLotIdentifier() {
        return lotIdentifier;
    }
    public void setLotIdentifier(String lotIdentifier) {
        this.lotIdentifier = lotIdentifier;
    }

    public boolean isInProduction() {
        return inProduction;
    }
    public void setInProduction(boolean inProduction) {
        this.inProduction = inProduction;
    }

    public boolean isInShipping() {
        return inShipping;
    }
    public void setInShipping(boolean inShipping) {
        this.inShipping = inShipping;
    }

    public boolean isInQuality() {
        return inQuality;
    }
    public void setInQuality(boolean inQuality) {
        this.inQuality = inQuality;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}
