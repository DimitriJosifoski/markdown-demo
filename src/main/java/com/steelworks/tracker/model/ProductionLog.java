package com.steelworks.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * ProductionLog — JPA entity mapped to the {@code production_logs} table.
 *
 * <p>Represents a single manufacturing-floor activity record. Each row ties
 * a {@link Lot} to a {@link ProductionLine} and optionally a {@link DefectType}.</p>
 *
 * <h3>Key fields for AC matching:</h3>
 * <ul>
 *   <li>{@code issueFlag} + {@code defectType} → used for defect counts (AC5) and trending (AC7).</li>
 *   <li>{@code productionLine}                 → line attribution (AC4).</li>
 *   <li>{@code sourceFile} / {@code sourceRowNumber} → source transparency (AC9).</li>
 * </ul>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC1  – Cross-referencing: joins to Lot via {@code lot_id}.</li>
 *   <li>AC4  – Line Attribution: every defect maps to a production line.</li>
 *   <li>AC5  – Production Line Ranking: counts defects per line.</li>
 *   <li>AC7  – Defect Trending: counts defect types over time.</li>
 *   <li>AC9  – Source Transparency: {@code sourceFile} tracks origin.</li>
 *   <li>AC11 – Consistency Check: detects same lot on different lines.</li>
 * </ul>
 */
@Entity
@Table(name = "production_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductionLog {

    /** Auto-generated surrogate PK. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date of production activity. */
    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    /**
     * Shift designation: "Day", "Swing", or "Night".
     * Validated by a DB CHECK constraint.
     */
    @Column(nullable = false, length = 20)
    private String shift;

    /** Target production volume for the shift. */
    @Column(name = "units_planned", nullable = false)
    private Integer unitsPlanned;

    /** Actual units produced (compared against planned for variance). */
    @Column(name = "units_actual", nullable = false)
    private Integer unitsActual;

    /** Minutes the line was stopped during this log period. */
    @Column(name = "downtime_minutes", nullable = false)
    private Integer downtimeMinutes;

    /**
     * Quick boolean flag: {@code true} if the production run had an issue.
     * Used by AC5 / AC6 / AC7 to filter "problematic" records without
     * needing to check for a non-null defect type.
     */
    @Column(name = "issue_flag", nullable = false)
    private Boolean issueFlag;

    /** Free-text notes from the shift supervisor. */
    @Column(name = "supervisor_notes", columnDefinition = "TEXT")
    private String supervisorNotes;

    // ── Source traceability (AC9) ────────────────────────────────────────

    /**
     * Name/path of the original file this record was imported from.
     * Nullable because manually entered records may not have a source file.
     * Supports AC9 (Source Transparency).
     */
    @Column(name = "source_file", length = 255)
    private String sourceFile;

    /**
     * Row number in the original source file.
     * Together with {@link #sourceFile}, gives analysts full traceability (AC9).
     */
    @Column(name = "source_row_number")
    private Integer sourceRowNumber;

    // ── Relationships ────────────────────────────────────────────────────

    /**
     * Many production logs belong to one production line.
     * {@code @JoinColumn} names the FK column in the DB.
     * {@code FetchType.LAZY} avoids loading the full ProductionLine object
     * unless explicitly accessed — improves query performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false)
    private ProductionLine productionLine;

    /**
     * Many production logs reference one lot (the batch being produced).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    /**
     * Optional link to the defect category recorded during this run.
     * NULL when {@code issueFlag} is {@code false}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_type_id")
    private DefectType defectType;
}
