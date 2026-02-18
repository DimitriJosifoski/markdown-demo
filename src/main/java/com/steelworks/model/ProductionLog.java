package com.steelworks.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Activity record for the manufacturing floor.
 * AC1: One of the three data sources joined by Lot ID.
 * AC4: Links each defect to a specific Production Line via timestamps.
 */
@Entity
@Table(name = "production_logs")
public class ProductionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "shift", nullable = false)
    private String shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false)
    private ProductionLine productionLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_type_id")
    private DefectType defectType;

    @Column(name = "units_planned", nullable = false)
    private Integer unitsPlanned;

    @Column(name = "units_actual", nullable = false)
    private Integer unitsActual;

    @Column(name = "downtime_minutes", nullable = false)
    private Integer downtimeMinutes;

    @Column(name = "issue_flag", nullable = false)
    private Boolean issueFlag;

    @Column(name = "supervisor_notes")
    private String supervisorNotes;

    public ProductionLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public ProductionLine getProductionLine() { return productionLine; }
    public void setProductionLine(ProductionLine productionLine) { this.productionLine = productionLine; }

    public Lot getLot() { return lot; }
    public void setLot(Lot lot) { this.lot = lot; }

    public DefectType getDefectType() { return defectType; }
    public void setDefectType(DefectType defectType) { this.defectType = defectType; }

    public Integer getUnitsPlanned() { return unitsPlanned; }
    public void setUnitsPlanned(Integer unitsPlanned) { this.unitsPlanned = unitsPlanned; }

    public Integer getUnitsActual() { return unitsActual; }
    public void setUnitsActual(Integer unitsActual) { this.unitsActual = unitsActual; }

    public Integer getDowntimeMinutes() { return downtimeMinutes; }
    public void setDowntimeMinutes(Integer downtimeMinutes) { this.downtimeMinutes = downtimeMinutes; }

    public Boolean getIssueFlag() { return issueFlag; }
    public void setIssueFlag(Boolean issueFlag) { this.issueFlag = issueFlag; }

    public String getSupervisorNotes() { return supervisorNotes; }
    public void setSupervisorNotes(String supervisorNotes) { this.supervisorNotes = supervisorNotes; }
}
