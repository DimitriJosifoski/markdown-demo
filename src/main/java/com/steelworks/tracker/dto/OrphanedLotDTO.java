package com.steelworks.tracker.dto;

/**
 * OrphanedLotDTO — DTO for the Unmatched Record Handling view (AC10).
 *
 * <p>Represents a lot that exists in the system but has NO production logs
 * and NO shipping logs — meaning it can't be cross-referenced with any
 * operational data. These are flagged as "Orphaned Data" for review.</p>
 *
 * @param lotIdentifier the original business lot ID
 * @param partNumber    the part/SKU associated with this lot
 * @param status        always "Orphaned Data" per AC10
 */
public record OrphanedLotDTO(
        String lotIdentifier,
        String partNumber,
        String status
) {
}
