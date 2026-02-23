package com.steelworks.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Consolidated view joining Production, Quality, and Shipping data for a single lot. AC1:
 * Cross-references all three data sources. AC9: Each section includes source file origin for
 * double-checking.
 */
public class ConsolidatedLotView {

    private String lotIdentifier;
    private String partNumber;
    private LocalDate createdDate;

    // Production data
    private List<String> associatedProductionLines;
    private Integer totalUnitsPlanned;
    private Integer totalUnitsActual;
    private Integer totalDowntimeMinutes;

    // Quality / Defect data
    private List<String> defectsFound;
    private boolean hasIssueFlag;

    // Shipping data
    private String shippingStatus;
    private LocalDate shipDate;
    private String customerName;

    // AC9: Source traceability
    private String productionSourceFile;
    private String shippingSourceFile;
    private String qualitySourceFile;

    public ConsolidatedLotView() {
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

    public LocalDate getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public List<String> getAssociatedProductionLines() {
        return associatedProductionLines;
    }
    public void setAssociatedProductionLines(List<String> associatedProductionLines) {
        this.associatedProductionLines = associatedProductionLines;
    }

    public Integer getTotalUnitsPlanned() {
        return totalUnitsPlanned;
    }
    public void setTotalUnitsPlanned(Integer totalUnitsPlanned) {
        this.totalUnitsPlanned = totalUnitsPlanned;
    }

    public Integer getTotalUnitsActual() {
        return totalUnitsActual;
    }
    public void setTotalUnitsActual(Integer totalUnitsActual) {
        this.totalUnitsActual = totalUnitsActual;
    }

    public Integer getTotalDowntimeMinutes() {
        return totalDowntimeMinutes;
    }
    public void setTotalDowntimeMinutes(Integer totalDowntimeMinutes) {
        this.totalDowntimeMinutes = totalDowntimeMinutes;
    }

    public List<String> getDefectsFound() {
        return defectsFound;
    }
    public void setDefectsFound(List<String> defectsFound) {
        this.defectsFound = defectsFound;
    }

    public boolean isHasIssueFlag() {
        return hasIssueFlag;
    }
    public void setHasIssueFlag(boolean hasIssueFlag) {
        this.hasIssueFlag = hasIssueFlag;
    }

    public String getShippingStatus() {
        return shippingStatus;
    }
    public void setShippingStatus(String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }
    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProductionSourceFile() {
        return productionSourceFile;
    }
    public void setProductionSourceFile(String productionSourceFile) {
        this.productionSourceFile = productionSourceFile;
    }

    public String getShippingSourceFile() {
        return shippingSourceFile;
    }
    public void setShippingSourceFile(String shippingSourceFile) {
        this.shippingSourceFile = shippingSourceFile;
    }

    public String getQualitySourceFile() {
        return qualitySourceFile;
    }
    public void setQualitySourceFile(String qualitySourceFile) {
        this.qualitySourceFile = qualitySourceFile;
    }
}
