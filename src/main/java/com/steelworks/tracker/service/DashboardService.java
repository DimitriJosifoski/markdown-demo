package com.steelworks.tracker.service;

import com.steelworks.tracker.dto.*;
import com.steelworks.tracker.model.Lot;
import com.steelworks.tracker.repository.LotRepository;
import com.steelworks.tracker.repository.ProductionLogRepository;
import com.steelworks.tracker.repository.ShippingLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DashboardService — Builds the aggregated data for the "Meeting Ready"
 * summary dashboard (ACs 5–8, 10–11).
 *
 * <p>This is the most complex service in the application. It orchestrates
 * multiple repository queries and transforms the raw data into DTOs that
 * the Thymeleaf template can render directly.</p>
 *
 * <h3>Responsibilities / AC mapping:</h3>
 * <ul>
 *   <li>AC5  – {@link #getLineRankings(LocalDate, LocalDate)} — production line ranking.</li>
 *   <li>AC6  – {@link #getShippingRisks()} — problematic shipped batches.</li>
 *   <li>AC7  – {@link #getDefectTrends(LocalDate, LocalDate)} — defect trending w/ arrows.</li>
 *   <li>AC8  – {@link #buildDashboard(String)} — time-grouping (Weekly/Daily/Monthly).</li>
 *   <li>AC10 – {@link #getOrphanedLots()} — unmatched "Orphaned Data" records.</li>
 *   <li>AC11 – {@link #getDataConflicts()} — lots on multiple lines.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductionLogRepository productionLogRepository;
    private final ShippingLogRepository shippingLogRepository;
    private final LotRepository lotRepository;

    /**
     * Constructor injection of all required repositories.
     */
    public DashboardService(ProductionLogRepository productionLogRepository,
                            ShippingLogRepository shippingLogRepository,
                            LotRepository lotRepository) {
        this.productionLogRepository = productionLogRepository;
        this.shippingLogRepository = shippingLogRepository;
        this.lotRepository = lotRepository;
    }

    // ========================================================================
    // Main orchestrator
    // ========================================================================

    /**
     * Build the complete dashboard DTO for a given time grouping (AC8).
     *
     * <p><b>AC8 — Default Time-Grouping:</b> If {@code timeGrouping} is null or
     * blank, it defaults to "WEEKLY". The controller passes the user's choice
     * from the toggle control.</p>
     *
     * <p><b>Algorithm:</b></p>
     * <ol>
     *   <li>Determine the reporting period (start/end dates) based on the grouping.</li>
     *   <li>Call each sub-method to populate its section of the dashboard.</li>
     *   <li>Bundle everything into a {@link DashboardDTO} record.</li>
     * </ol>
     *
     * @param timeGrouping "DAILY", "WEEKLY", or "MONTHLY" (defaults to "WEEKLY")
     * @return the fully populated dashboard DTO
     *
     * <p><b>Time complexity:</b> Dominated by the individual queries — see each method.</p>
     */
    public DashboardDTO buildDashboard(String timeGrouping) {
        // ── AC8: default to WEEKLY if not specified ──────────────────────
        if (timeGrouping == null || timeGrouping.isBlank()) {
            timeGrouping = "WEEKLY";
        }
        timeGrouping = timeGrouping.toUpperCase(); // Normalize for comparison

        // ── Compute the date range based on the grouping ────────────────
        LocalDate today = LocalDate.now();
        LocalDate periodStart;
        LocalDate periodEnd = today;

        switch (timeGrouping) {
            case "DAILY":
                // Show data for today only.
                periodStart = today;
                break;
            case "MONTHLY":
                // From the 1st of the current month to today.
                periodStart = today.with(TemporalAdjusters.firstDayOfMonth());
                break;
            case "WEEKLY":
            default:
                // From the most recent Monday to today (ISO week starts on Monday).
                periodStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
        }

        // ── Assemble each dashboard section ─────────────────────────────
        List<LineDefectCountDTO> lineRankings = getLineRankings(periodStart, periodEnd);
        List<ShippingRiskDTO> shippingRisks = getShippingRisks();
        List<DefectTrendDTO> defectTrends = getDefectTrends(periodStart, periodEnd);
        List<OrphanedLotDTO> orphanedLots = getOrphanedLots();
        List<DataConflictDTO> dataConflicts = getDataConflicts();

        return new DashboardDTO(
                lineRankings,
                shippingRisks,
                defectTrends,
                orphanedLots,
                dataConflicts,
                periodStart,
                periodEnd,
                timeGrouping
        );
    }

    // ========================================================================
    // AC5 — Production Line Ranking
    // ========================================================================

    /**
     * Rank production lines by total defect count within a date range.
     *
     * <p>Queries all production logs where {@code issue_flag = true} in the
     * given range, groups by line name, and returns the count in descending order.</p>
     *
     * @param start period start (inclusive)
     * @param end   period end (inclusive)
     * @return ranked list of lines with defect counts
     *
     * <p><b>Time complexity:</b> O(n) where n = production logs in the date range
     * (the GROUP BY is done by PostgreSQL using a hash aggregate).</p>
     */
    public List<LineDefectCountDTO> getLineRankings(LocalDate start, LocalDate end) {
        // The repository returns raw Object[] arrays: [lineName, count]
        List<Object[]> raw = productionLogRepository.countDefectsByLineInRange(start, end);

        List<LineDefectCountDTO> rankings = new ArrayList<>();
        int rank = 1; // Ordinal rank counter
        for (Object[] row : raw) {
            String lineName = (String) row[0];
            long count = (Long) row[1];
            rankings.add(new LineDefectCountDTO(lineName, count, rank));
            rank++;
        }
        return rankings;
    }

    // ========================================================================
    // AC6 — Shipping Risk Alert
    // ========================================================================

    /**
     * Find all "Problematic Shipped Batches": lots with defects that have shipped.
     *
     * <p>This is a high-priority alert. The query joins ShippingLog → Lot →
     * ProductionLog → DefectType to find shipped lots that had issues.</p>
     *
     * @return list of shipping risk DTOs sorted by ship date (most recent first)
     *
     * <p><b>Time complexity:</b> O(s × p) worst case where s = shipping logs,
     * p = production logs; mitigated by DB indexes on lot_id.</p>
     */
    public List<ShippingRiskDTO> getShippingRisks() {
        List<Object[]> raw = shippingLogRepository.findProblematicShippedBatches();

        return raw.stream().map(row -> new ShippingRiskDTO(
                (String) row[0],       // lotIdentifier
                (String) row[1],       // customerName
                (LocalDate) row[2],    // shipDate
                (String) row[3],       // defectName
                (String) row[4]        // severity
        )).collect(Collectors.toList());
    }

    // ========================================================================
    // AC7 — Defect Trending
    // ========================================================================

    /**
     * Compare defect-type frequencies between the current period and the
     * previous period of the same length to derive trend direction.
     *
     * <p><b>Algorithm:</b></p>
     * <ol>
     *   <li>Query defect counts for the current period (start → end).</li>
     *   <li>Compute the "previous period" as a range of the same length
     *       immediately before the current period.</li>
     *   <li>Query defect counts for the previous period.</li>
     *   <li>For each defect type, compare counts to decide the trend arrow:
     *       <ul>
     *         <li>Current &gt; Previous → "UP" (▲ getting worse)</li>
     *         <li>Current &lt; Previous → "DOWN" (▼ improving)</li>
     *         <li>Current == Previous → "FLAT" (— stable)</li>
     *         <li>Not in previous period → "NEW" (first occurrence)</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @param currentStart start of the current reporting period
     * @param currentEnd   end of the current reporting period
     * @return list of defect trend DTOs with direction indicators
     *
     * <p><b>Time complexity:</b> O(n + m) where n = current-period logs,
     * m = previous-period logs, plus O(d) to iterate distinct defect types.</p>
     */
    public List<DefectTrendDTO> getDefectTrends(LocalDate currentStart, LocalDate currentEnd) {
        // ── Step 1: Query current period counts ─────────────────────────
        List<Object[]> currentRaw = productionLogRepository
                .countDefectsByTypeInRange(currentStart, currentEnd);

        // ── Step 2: Compute previous period of the same length ──────────
        // Example: if current is Mon–Sun (7 days), previous is last Mon–last Sun.
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(currentStart, currentEnd);
        // Add 1 because the range is inclusive on both sides.
        LocalDate previousStart = currentStart.minusDays(daysBetween + 1);
        LocalDate previousEnd = currentStart.minusDays(1);

        // ── Step 3: Query previous period counts ────────────────────────
        List<Object[]> previousRaw = productionLogRepository
                .countDefectsByTypeInRange(previousStart, previousEnd);

        // ── Step 4: Build a lookup map for previous-period counts ───────
        // Key: defectName, Value: count.
        // Using a HashMap gives O(1) lookup per defect type.
        Map<String, Long> previousMap = new HashMap<>();
        for (Object[] row : previousRaw) {
            previousMap.put((String) row[0], (Long) row[2]);
        }

        // ── Step 5: Build trend DTOs by comparing current vs previous ───
        List<DefectTrendDTO> trends = new ArrayList<>();
        for (Object[] row : currentRaw) {
            String defectName = (String) row[0];
            String severity = (String) row[1];
            long currentCount = (Long) row[2];

            // Look up previous count; default to 0 if defect didn't exist before.
            long previousCount = previousMap.getOrDefault(defectName, 0L);

            // Determine the direction indicator.
            String direction;
            if (previousCount == 0 && currentCount > 0) {
                direction = "NEW";   // Defect type appeared for the first time
            } else if (currentCount > previousCount) {
                direction = "UP";    // Getting worse
            } else if (currentCount < previousCount) {
                direction = "DOWN";  // Improving
            } else {
                direction = "FLAT";  // No change
            }

            trends.add(new DefectTrendDTO(
                    defectName, severity, currentCount, previousCount, direction
            ));
        }

        return trends;
    }

    // ========================================================================
    // AC10 — Orphaned Data
    // ========================================================================

    /**
     * Find lots that have no production logs AND no shipping logs.
     *
     * <p>Per AC10, these are flagged as "Orphaned Data" rather than excluded.
     * This ensures analysts are aware of data gaps and can investigate.</p>
     *
     * @return list of orphaned lot DTOs
     *
     * <p><b>Time complexity:</b> O(n) scan of the lots table with sub-selects
     * on the relationship tables.</p>
     */
    public List<OrphanedLotDTO> getOrphanedLots() {
        List<Lot> orphans = lotRepository.findOrphanedLots();
        return orphans.stream()
                .map(lot -> new OrphanedLotDTO(
                        lot.getLotIdentifier(),
                        lot.getPartNumber(),
                        "Orphaned Data" // Static status per AC10
                ))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // AC11 — Data Conflicts
    // ========================================================================

    /**
     * Find lots that appear on more than one production line.
     *
     * <p>Per AC11, these are flagged as "Data Conflict" for manual review.
     * The most common cause is a data-entry error or a lot being split
     * across lines without proper tracking.</p>
     *
     * @return list of data conflict DTOs
     *
     * <p><b>Time complexity:</b> O(n) where n = total production logs
     * (the GROUP BY + HAVING is computed by PostgreSQL).</p>
     */
    public List<DataConflictDTO> getDataConflicts() {
        List<Object[]> raw = productionLogRepository.findLotsWithMultipleLines();
        return raw.stream()
                .map(row -> new DataConflictDTO(
                        (String) row[0],    // lotIdentifier
                        (Long) row[1]       // distinctLineCount
                ))
                .collect(Collectors.toList());
    }
}
