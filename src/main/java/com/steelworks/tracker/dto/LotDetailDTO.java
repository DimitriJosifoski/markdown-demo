package com.steelworks.tracker.dto;

import java.time.LocalDate;

/**
 * LotDetailDTO — DTO for the Lot Detail / Lookup view (AC1, AC3, AC4, AC9).
 *
 * <p>Provides a consolidated view of a single lot: its identifiers, shipping
 * status, assigned production line, and source traceability information.</p>
 *
 * @param lotIdentifier     the original business lot ID
 * @param partNumber        the SKU/part number
 * @param shippingStatus    "Shipped" or "In Inventory" (AC3)
 * @param productionLine    the line this lot was produced on (AC4); may be "Multiple (Conflict)" (AC11)
 * @param defectSummary     comma-separated list of defect names found on this lot
 * @param sourceFile        original source file name for traceability (AC9)
 * @param sourceRowNumber   row number in source file (AC9)
 */
public record LotDetailDTO(
        String lotIdentifier,
        String partNumber,
        String shippingStatus,
        String productionLine,
        String defectSummary,
        String sourceFile,
        Integer sourceRowNumber
) {
}
