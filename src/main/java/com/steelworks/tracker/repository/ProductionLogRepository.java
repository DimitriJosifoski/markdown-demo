package com.steelworks.tracker.repository;

import com.steelworks.tracker.model.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ProductionLogRepository — Spring Data JPA repository for {@link ProductionLog}.
 *
 * <p>Contains custom queries that power the dashboard views required by the ACs:</p>
 * <ul>
 *   <li>AC5  – Production line ranking (defect counts per line in a date range).</li>
 *   <li>AC7  – Defect trending (defect-type counts per period).</li>
 *   <li>AC11 – Consistency check (lots appearing on multiple lines).</li>
 * </ul>
 */
@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {

    /**
     * Find all production logs for a specific lot.
     *
     * @param lotId the lot's surrogate PK
     * @return list of production logs
     *
     * <p><b>Time complexity:</b> O(log n + k) where k = result count, using the
     * {@code idx_production_lot_id} index.</p>
     */
    List<ProductionLog> findByLotId(Long lotId);

    /**
     * Find production logs within a date range.
     *
     * @param start inclusive start date
     * @param end   inclusive end date
     * @return matching logs, ordered by date
     *
     * <p><b>Time complexity:</b> O(log n + k) via {@code idx_production_date}.</p>
     */
    List<ProductionLog> findByProductionDateBetween(LocalDate start, LocalDate end);

    /**
     * AC5 — Production Line Ranking: count defects per production line in a date range.
     *
     * <p>Returns an array of [lineName, defectCount] rows sorted by count descending.
     * Only rows where {@code issueFlag = true} are counted.</p>
     *
     * @param start start of the period (inclusive)
     * @param end   end of the period (inclusive)
     * @return list of Object arrays: [String lineName, Long count]
     *
     * <p><b>Time complexity:</b> O(n) scan of production_logs in the date range,
     * then O(m log m) sort where m = number of distinct lines.</p>
     */
    @Query("SELECT pl.lineName, COUNT(p) " +
           "FROM ProductionLog p JOIN p.productionLine pl " +
           "WHERE p.issueFlag = true " +
           "  AND p.productionDate BETWEEN :start AND :end " +
           "GROUP BY pl.lineName " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> countDefectsByLineInRange(@Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    /**
     * AC7 — Defect Trending: count occurrences of each defect type in a date range.
     *
     * <p>Returns [defectName, severity, count] tuples so the service layer can
     * compare the current period against the previous period to derive the
     * "up/down" trend indicator.</p>
     *
     * @param start start of the period (inclusive)
     * @param end   end of the period (inclusive)
     * @return list of Object arrays: [String defectName, String severity, Long count]
     */
    @Query("SELECT dt.defectName, dt.severity, COUNT(p) " +
           "FROM ProductionLog p JOIN p.defectType dt " +
           "WHERE p.issueFlag = true " +
           "  AND p.productionDate BETWEEN :start AND :end " +
           "GROUP BY dt.defectName, dt.severity " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> countDefectsByTypeInRange(@Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    /**
     * AC11 — Consistency Check: find lots that appear on more than one production line.
     *
     * <p>Groups production logs by lot ID and checks for distinct line counts &gt; 1.
     * Returns [lotIdentifier, lineCount] tuples for flagging as "Data Conflict".</p>
     *
     * @return list of Object arrays: [String lotIdentifier, Long distinctLineCount]
     */
    @Query("SELECT l.lotIdentifier, COUNT(DISTINCT pl.lineName) " +
           "FROM ProductionLog p " +
           "JOIN p.lot l " +
           "JOIN p.productionLine pl " +
           "GROUP BY l.lotIdentifier " +
           "HAVING COUNT(DISTINCT pl.lineName) > 1")
    List<Object[]> findLotsWithMultipleLines();

    /**
     * Find all production logs that have an issue for a given lot.
     * Used by AC6 (Shipping Risk) to see if a shipped lot had defects.
     *
     * @param lotId the lot's surrogate PK
     * @return list of flagged production logs
     */
    List<ProductionLog> findByLotIdAndIssueFlagTrue(Long lotId);
}
