package com.steelworks.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DefectType — JPA entity mapped to the {@code defect_types} table.
 *
 * <p>Standardises the names and severities of manufacturing issues so that
 * trending reports (AC7) are consistent across production logs.</p>
 *
 * <h3>Severity levels (enforced by a DB CHECK constraint):</h3>
 * <ul>
 *   <li><b>Critical</b> – safety or customer-impact risk.</li>
 *   <li><b>Major</b>    – significant quality degradation.</li>
 *   <li><b>Minor</b>    – cosmetic or low-impact deviation.</li>
 * </ul>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC6 – Shipping Risk: critical defects on shipped lots are flagged.</li>
 *   <li>AC7 – Defect Trending: frequency comparison uses defect types.</li>
 * </ul>
 */
@Entity
@Table(name = "defect_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DefectType {

    /** Auto-generated surrogate PK. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Short code that uniquely identifies the defect category (e.g., "CHG-DLY").
     */
    @Column(name = "defect_code", nullable = false, unique = true, length = 20)
    private String defectCode;

    /**
     * Human-readable name shown in dashboards (e.g., "Changeover Delay").
     */
    @Column(name = "defect_name", nullable = false, length = 100)
    private String defectName;

    /**
     * Severity classification: "Critical", "Major", or "Minor".
     * The DB has a CHECK constraint guaranteeing only these three values.
     */
    @Column(nullable = false, length = 20)
    private String severity;

    /** Optional longer description of the defect. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Back-reference to every production log that recorded this defect.
     * "mappedBy" points to the {@link ProductionLog#defectType} field.
     */
    @OneToMany(mappedBy = "defectType")
    private List<ProductionLog> productionLogs = new ArrayList<>();
}
