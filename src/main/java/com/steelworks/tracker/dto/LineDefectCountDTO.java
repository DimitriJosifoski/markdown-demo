package com.steelworks.tracker.dto;

/**
 * LineDefectCountDTO — Data Transfer Object for the Production Line Ranking view (AC5).
 *
 * <p>A DTO is a simple data carrier that moves information between layers
 * (Service → Controller → Template) without exposing internal JPA entities.
 * Using DTOs prevents accidental lazy-loading exceptions in the view layer.</p>
 *
 * <p><b>Java Record:</b> Records are immutable data classes introduced in Java 16.
 * The compiler auto-generates: constructor, getters (e.g., {@code lineName()}),
 * {@code equals()}, {@code hashCode()}, and {@code toString()}.</p>
 *
 * @param lineName   the human-readable production line name (e.g., "Line 1")
 * @param defectCount total number of defects recorded for this line in the period
 * @param rank        ordinal position in the ranking (1 = worst)
 */
public record LineDefectCountDTO(
        String lineName,
        long defectCount,
        int rank
) {
    // No body needed — the record declaration above generates everything.
}
