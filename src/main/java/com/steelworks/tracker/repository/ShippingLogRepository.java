package com.steelworks.tracker.repository;

import com.steelworks.tracker.model.ShippingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ShippingLogRepository — Spring Data JPA repository for {@link ShippingLog}.
 *
 * <p>Provides queries that support:</p>
 * <ul>
 *   <li>AC3 – Shipping Status Logic: look up by lot to determine "Shipped" vs "In Inventory".</li>
 *   <li>AC6 – Shipping Risk Alert: find shipped lots that had critical defects.</li>
 * </ul>
 */
@Repository
public interface ShippingLogRepository extends JpaRepository<ShippingLog, Long> {

    /**
     * Find all shipping records for a given lot.
     * A non-empty result with a valid {@code shipDate} means the lot is "Shipped" (AC3).
     *
     * @param lotId the lot's surrogate PK
     * @return list of shipping logs (may be empty → lot is "In Inventory")
     *
     * <p><b>Time complexity:</b> O(log n + k) via {@code idx_shipping_lot_id}.</p>
     */
    List<ShippingLog> findByLotId(Long lotId);

    /**
     * AC6 — Shipping Risk Alert: find all lots that were shipped AND have production
     * defects flagged as issues.
     *
     * <p>Uses a JPQL join across ShippingLog → Lot → ProductionLog → DefectType.
     * Returns [lotIdentifier, customerName, shipDate, defectName, severity] tuples.</p>
     *
     * <p>Only rows where the shipment status is 'Shipped' and the production log
     * has {@code issueFlag = true} are included.  Results are ordered most-recent first.</p>
     *
     * @return list of Object arrays with risk details
     *
     * <p><b>Time complexity:</b> O(s × p) in the worst case where s = shipped logs,
     * p = production logs per lot; mitigated by DB indexes on lot_id.</p>
     */
    @Query("SELECT l.lotIdentifier, c.customerName, s.shipDate, dt.defectName, dt.severity " +
           "FROM ShippingLog s " +
           "JOIN s.lot l " +
           "JOIN s.customer c " +
           "JOIN l.productionLogs p " +
           "JOIN p.defectType dt " +
           "WHERE s.shipStatus = 'Shipped' " +
           "  AND p.issueFlag = true " +
           "ORDER BY s.shipDate DESC")
    List<Object[]> findProblematicShippedBatches();

    /**
     * Find shipping logs within a date range.
     *
     * @param start inclusive start date
     * @param end   inclusive end date
     * @return matching logs sorted by date
     */
    List<ShippingLog> findByShipDateBetween(LocalDate start, LocalDate end);

    /**
     * Check whether a lot has any "Shipped" record with a valid ship date.
     * Returns true if at least one exists — used to derive the AC3 status.
     *
     * @param lotId the lot's surrogate PK
     * @return true if the lot has been shipped
     */
    boolean existsByLotIdAndShipStatus(Long lotId, String shipStatus);
}
