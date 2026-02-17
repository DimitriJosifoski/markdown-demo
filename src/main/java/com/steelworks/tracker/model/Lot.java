package com.steelworks.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lot — JPA entity mapped to the {@code lots} table.
 *
 * <p>The <b>central anchor record</b> of the data model. Every production log
 * and shipping log references a Lot via its ID. The {@code lotIdentifier}
 * field holds the "business key" (e.g., "LOT-20260112-001") that is used for
 * fuzzy matching (AC2).</p>
 *
 * <h3>Fuzzy matching strategy (AC2):</h3>
 * <p>A companion field {@code normalizedLotId} stores a stripped version of the
 * identifier (uppercase, hyphens/spaces removed). The service layer normalizes
 * incoming lookup strings the same way and queries on this column for O(1)
 * hash-based or O(log n) index-based matches.</p>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC1  – Cross-referencing: Lot is the join key across data sources.</li>
 *   <li>AC2  – Fuzzy matching: see {@code normalizedLotId}.</li>
 *   <li>AC3  – Shipping status: derived from whether a ShippingLog exists.</li>
 *   <li>AC10 – Orphaned data: lots with no production or shipping records.</li>
 *   <li>AC11 – Data conflict: lots linked to multiple production lines.</li>
 * </ul>
 */
@Entity
@Table(name = "lots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Lot {

    /** Auto-generated surrogate PK. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The original, human-entered lot identifier (e.g., "LOT-20260112-001").
     * This value is <em>exactly</em> as it appears in the source file.
     */
    @Column(name = "lot_identifier", nullable = false, unique = true, length = 50)
    private String lotIdentifier;

    /**
     * Normalised version of {@link #lotIdentifier}: uppercased with hyphens,
     * spaces, and underscores stripped.  Used for O(1) fuzzy lookups (AC2).
     *
     * <p>Example: "LOT-20260112-001" → "LOT20260112001".</p>
     */
    @Column(name = "normalized_lot_id", length = 50)
    private String normalizedLotId;

    /**
     * The SKU or part number being manufactured in this lot.
     */
    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    /**
     * Date the lot record was created (defaults to today in the DB).
     */
    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    // ── Relationships ────────────────────────────────────────────────────

    /**
     * One lot can appear in many production logs (different shifts, dates).
     */
    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionLog> productionLogs = new ArrayList<>();

    /**
     * One lot can have zero or more shipping log entries (partial shipments, etc.).
     */
    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingLog> shippingLogs = new ArrayList<>();
}
