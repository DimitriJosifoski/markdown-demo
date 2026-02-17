package com.steelworks.tracker.repository;

import com.steelworks.tracker.model.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LotRepository — Spring Data JPA repository for the {@link Lot} entity.
 *
 * <p>By extending {@link JpaRepository}, Spring auto-generates common CRUD methods
 * at runtime (save, findById, findAll, delete, etc.).<br>
 * We only need to declare additional query methods that are specific to our ACs.</p>
 *
 * <h3>How query derivation works:</h3>
 * <p>Spring Data parses the method name (e.g., {@code findByNormalizedLotId})
 * and generates a SQL WHERE clause automatically:
 * {@code SELECT * FROM lots WHERE normalized_lot_id = ?}</p>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC2  – Fuzzy Matching: {@link #findByNormalizedLotId(String)}.</li>
 *   <li>AC10 – Orphaned Data: {@link #findOrphanedLots()}.</li>
 * </ul>
 */
@Repository  // Marks this as a Spring-managed persistence bean. (optional for JpaRepository but explicit)
public interface LotRepository extends JpaRepository<Lot, Long> {

    /**
     * Find a lot by its original (exact) business identifier.
     *
     * @param lotIdentifier the exact lot string (e.g., "LOT-20260112-001")
     * @return Optional containing the lot, or empty if not found
     *
     * <p><b>Time complexity:</b> O(log n) — uses the UNIQUE index on {@code lot_identifier}.</p>
     */
    Optional<Lot> findByLotIdentifier(String lotIdentifier);

    /**
     * Find a lot by its normalised identifier for <b>fuzzy matching</b> (AC2).
     *
     * <p>The caller normalises the search term (strip hyphens, uppercase),
     * and we look it up against the pre-normalised column.  This avoids
     * expensive LIKE / regex scans at query time.</p>
     *
     * @param normalizedLotId the normalised string (e.g., "LOT20260112001")
     * @return Optional containing the lot, or empty
     *
     * <p><b>Time complexity:</b> O(log n) with an index on {@code normalized_lot_id}.</p>
     */
    Optional<Lot> findByNormalizedLotId(String normalizedLotId);

    /**
     * AC10 — Find "Orphaned" lots: lots that have NO production logs AND NO shipping logs.
     *
     * <p>Uses a JPQL query with LEFT JOINs and IS EMPTY checks.
     * These lots exist in the system but can't be cross-referenced with any data source.</p>
     *
     * @return list of lots with no related production or shipping data
     *
     * <p><b>Time complexity:</b> O(n) over the lots table with nested sub-selects.</p>
     */
    @Query("SELECT l FROM Lot l WHERE l.productionLogs IS EMPTY AND l.shippingLogs IS EMPTY")
    List<Lot> findOrphanedLots();

    /**
     * Find all lots whose normalised ID matches any in the given list.
     * Useful for bulk fuzzy-match imports.
     *
     * @param normalizedIds list of normalised ID strings
     * @return matching lots
     */
    List<Lot> findByNormalizedLotIdIn(List<String> normalizedIds);
}
