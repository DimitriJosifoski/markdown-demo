package com.steelworks.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductionLine — JPA entity mapped to the {@code production_lines} table.
 *
 * <p>Represents a physical manufacturing line on the factory floor (e.g., "Line 1").
 * Each line belongs to a {@code department} and can host many {@link ProductionLog} entries.</p>
 *
 * <h3>Lombok annotations (generate boilerplate at compile-time):</h3>
 * <ul>
 *   <li>{@code @Getter}           – creates public getter for every field.</li>
 *   <li>{@code @Setter}           – creates public setter for every field.</li>
 *   <li>{@code @NoArgsConstructor} – creates a no-arg constructor (required by JPA).</li>
 *   <li>{@code @AllArgsConstructor} – creates a constructor with all fields.</li>
 * </ul>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC4  – Line Attribution: defects are mapped to a production line.</li>
 *   <li>AC5  – Production Line Ranking: lines are ranked by defect count.</li>
 *   <li>AC11 – Consistency Check: conflicts arise when a lot maps to two different lines.</li>
 * </ul>
 */
@Entity                                       // Marks this class as a JPA-managed entity.
@Table(name = "production_lines")             // Maps to the "production_lines" DB table.
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductionLine {

    /**
     * Auto-generated surrogate primary key.
     * {@code IDENTITY} strategy tells Hibernate to let PostgreSQL's
     * {@code SERIAL} column handle ID generation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable line name (e.g., "Line 1", "Line 4").
     * Unique constraint mirrors the DB schema's {@code UNIQUE} on {@code line_name}.
     */
    @Column(name = "line_name", nullable = false, unique = true, length = 50)
    private String lineName;

    /**
     * Functional department the line belongs to (e.g., "Assembly", "Finishing").
     */
    @Column(nullable = false, length = 100)
    private String department;

    /**
     * One-to-many relationship: one production line can have many production logs.
     * {@code mappedBy = "productionLine"} means the FK column lives on the
     * {@link ProductionLog} side (the "many" side owns the relationship).
     *
     * {@code CascadeType.ALL} propagates persist/merge/remove to child logs.
     * {@code orphanRemoval = true} deletes a log if it's removed from this list.
     */
    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionLog> productionLogs = new ArrayList<>();
}
