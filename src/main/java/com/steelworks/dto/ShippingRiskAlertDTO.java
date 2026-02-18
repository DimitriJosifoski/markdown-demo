package com.steelworks.dto;

import java.time.LocalDate;

/**
 * DTO for shipping risk alerts.
 * AC6: Identifies "Problematic Shipped Batches" — lots with critical defects
 *      that have an associated ship date.
 */
public class ShippingRiskAlertDTO {

    private String lotIdentifier;
    private String defectName;
    private String defectSeverity;
    private LocalDate shipDate;
    private String customerName;
    private String productionLineName;

    public ShippingRiskAlertDTO() {}

    public String getLotIdentifier() { return lotIdentifier; }
    public void setLotIdentifier(String lotIdentifier) { this.lotIdentifier = lotIdentifier; }

    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }

    public String getDefectSeverity() { return defectSeverity; }
    public void setDefectSeverity(String defectSeverity) { this.defectSeverity = defectSeverity; }

    public LocalDate getShipDate() { return shipDate; }
    public void setShipDate(LocalDate shipDate) { this.shipDate = shipDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getProductionLineName() { return productionLineName; }
    public void setProductionLineName(String productionLineName) { this.productionLineName = productionLineName; }
}
