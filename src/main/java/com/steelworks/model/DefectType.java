package com.steelworks.model;

import jakarta.persistence.*;

/**
 * Standardizes defect/issue names for trending reports.
 * AC7: Defect trending uses this to categorize and compare frequencies.
 */
@Entity
@Table(name = "defect_types")
public class DefectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "defect_code", nullable = false, unique = true)
    private String defectCode;

    @Column(name = "defect_name", nullable = false)
    private String defectName;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "description")
    private String description;

    public DefectType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDefectCode() { return defectCode; }
    public void setDefectCode(String defectCode) { this.defectCode = defectCode; }

    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
