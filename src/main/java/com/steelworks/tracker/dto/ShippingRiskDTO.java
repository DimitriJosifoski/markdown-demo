package com.steelworks.tracker.dto;

import java.time.LocalDate;

/**
 * ShippingRiskDTO — DTO for the Shipping Risk Alert view (AC6).
 *
 * <p>Represents a "Problematic Shipped Batch": a lot that has critical defects
 * AND has already been shipped to a customer.</p>
 *
 * @param lotIdentifier the business lot ID (e.g., "LOT-20260112-001")
 * @param customerName  the customer who received the shipment
 * @param shipDate      date the batch left the facility
 * @param defectName    the type of issue found (e.g., "Surface Crack")
 * @param severity      defect severity: "Critical", "Major", or "Minor"
 */
public record ShippingRiskDTO(
        String lotIdentifier,
        String customerName,
        LocalDate shipDate,
        String defectName,
        String severity
) {
}
