package com.steelworks.tracker.dto;

/**
 * DefectTrendDTO — DTO for the Defect Trending view (AC7).
 *
 * <p>Shows whether a defect type has increased or decreased in frequency
 * compared to the previous period. The {@code trendDirection} field drives
 * the visual up/down arrow indicator in the Thymeleaf template.</p>
 *
 * <h3>Trend direction values:</h3>
 * <ul>
 *   <li>{@code "UP"}   – frequency increased → red up arrow ▲</li>
 *   <li>{@code "DOWN"} – frequency decreased → green down arrow ▼</li>
 *   <li>{@code "FLAT"} – no change → grey dash —</li>
 *   <li>{@code "NEW"}  – defect appeared for the first time in this period</li>
 * </ul>
 *
 * @param defectName      human-readable defect category name
 * @param severity        defect severity level
 * @param currentCount    occurrences in the current period
 * @param previousCount   occurrences in the previous (comparison) period
 * @param trendDirection  "UP", "DOWN", "FLAT", or "NEW"
 */
public record DefectTrendDTO(
        String defectName,
        String severity,
        long currentCount,
        long previousCount,
        String trendDirection
) {
}
