package com.steelworks.tracker.dto;

/**
 * DataConflictDTO — DTO for the Consistency Check view (AC11).
 *
 * <p>Flags lots that appear on more than one production line, indicating a
 * "Data Conflict" that requires manual review. For example, if "LOT-001" has
 * production records on both "Line 1" and "Line 3", this is suspicious.</p>
 *
 * @param lotIdentifier    the business lot ID with the conflict
 * @param distinctLineCount how many different production lines reference this lot
 */
public record DataConflictDTO(
        String lotIdentifier,
        long distinctLineCount
) {
}
