package com.steelworks.dto;

import com.steelworks.enums.ShipStatus;

/**
 * Output DTO for lot search results. AC1: Contains cross-referenced data from Production, Quality,
 * and Shipping sources. AC3: Includes computed shipping status. AC9: Includes source file/location
 * for traceability.
 */
public class LotSearchResult {

    private Long lotId;
    private String lotIdentifier;
    private String partNumber;
    private String productionLineName;
    private ShipStatus shippingStatus;
    private String defectName;
    private String defectSeverity;
    private boolean hasDataConflict;
    private String sourceReference;

    public LotSearchResult() {
    }

    public Long getLotId() {
        return lotId;
    }
    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public String getLotIdentifier() {
        return lotIdentifier;
    }
    public void setLotIdentifier(String lotIdentifier) {
        this.lotIdentifier = lotIdentifier;
    }

    public String getPartNumber() {
        return partNumber;
    }
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getProductionLineName() {
        return productionLineName;
    }
    public void setProductionLineName(String productionLineName) {
        this.productionLineName = productionLineName;
    }

    public ShipStatus getShippingStatus() {
        return shippingStatus;
    }
    public void setShippingStatus(ShipStatus shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public String getDefectName() {
        return defectName;
    }
    public void setDefectName(String defectName) {
        this.defectName = defectName;
    }

    public String getDefectSeverity() {
        return defectSeverity;
    }
    public void setDefectSeverity(String defectSeverity) {
        this.defectSeverity = defectSeverity;
    }

    public boolean isHasDataConflict() {
        return hasDataConflict;
    }
    public void setHasDataConflict(boolean hasDataConflict) {
        this.hasDataConflict = hasDataConflict;
    }

    public String getSourceReference() {
        return sourceReference;
    }
    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }
}
