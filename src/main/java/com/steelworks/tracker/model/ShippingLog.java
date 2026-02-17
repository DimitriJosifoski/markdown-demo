package com.steelworks.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * ShippingLog — JPA entity mapped to the {@code shipping_logs} table.
 *
 * <p>Records every shipment that leaves the facility. The presence (or absence)
 * of a shipping log for a {@link Lot} determines whether the lot is marked
 * "Shipped" or "In Inventory" (AC3).</p>
 *
 * <h3>Ship status values (DB CHECK constraint):</h3>
 * <ul>
 *   <li><b>Shipped</b>  – lot has left the building.</li>
 *   <li><b>On Hold</b>  – blocked, see {@code holdReason}.</li>
 *   <li><b>Partial</b>  – only part of the lot was shipped.</li>
 * </ul>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC1  – Cross-referencing: joins to Lot via {@code lot_id}.</li>
 *   <li>AC3  – Shipping Status: determines "Shipped" vs "In Inventory".</li>
 *   <li>AC6  – Shipping Risk Alert: critical defects on shipped lots.</li>
 *   <li>AC9  – Source Transparency: {@code sourceFile} tracks origin.</li>
 * </ul>
 */
@Entity
@Table(name = "shipping_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShippingLog {

    /** Auto-generated surrogate PK. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date the batch physically left the facility. */
    @Column(name = "ship_date", nullable = false)
    private LocalDate shipDate;

    /** External sales order reference number from the buyer. */
    @Column(name = "sales_order_number", nullable = false, length = 50)
    private String salesOrderNumber;

    /** US state code for destination (e.g., "IN", "CA"). */
    @Column(name = "destination_state", nullable = false, length = 2)
    private String destinationState;

    /** Logistics carrier name (e.g., "UPS", "FedEx", "XPO"). */
    @Column(length = 100)
    private String carrier;

    /** Bill of Lading number — unique per shipment (DB UNIQUE constraint). */
    @Column(name = "bol_number", nullable = false, unique = true, length = 100)
    private String bolNumber;

    /** Carrier tracking / PRO number for shipment visibility. */
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    /** Physical count of units shipped. Must be &gt; 0 (DB CHECK). */
    @Column(name = "qty_shipped", nullable = false)
    private Integer qtyShipped;

    /**
     * Current shipment status: "Shipped", "On Hold", or "Partial".
     */
    @Column(name = "ship_status", nullable = false, length = 50)
    private String shipStatus;

    /** Reason the lot is on hold (only populated when shipStatus = "On Hold"). */
    @Column(name = "hold_reason", columnDefinition = "TEXT")
    private String holdReason;

    /** General logistics notes or comments. */
    @Column(name = "shipping_notes", columnDefinition = "TEXT")
    private String shippingNotes;

    // ── Source traceability (AC9) ────────────────────────────────────────

    /**
     * Original source file this record was imported from.
     * Supports AC9 (Source Transparency) so analysts can double-check data.
     */
    @Column(name = "source_file", length = 255)
    private String sourceFile;

    /** Row number in the original source file. */
    @Column(name = "source_row_number")
    private Integer sourceRowNumber;

    // ── Relationships ────────────────────────────────────────────────────

    /**
     * The lot this shipment belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    /**
     * The customer receiving this shipment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
