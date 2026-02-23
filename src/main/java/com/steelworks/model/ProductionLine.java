package com.steelworks.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalized production line asset table. Tracks line-specific performance within the facility.
 * Maps to the "production_lines" table.
 */
@Entity
@Table(name = "production_lines")
public class ProductionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_name", nullable = false, unique = true)
    private String lineName;

    @Column(name = "department", nullable = false)
    private String department;

    @OneToMany(mappedBy = "productionLine", fetch = FetchType.LAZY)
    private List<ProductionLog> productionLogs = new ArrayList<>();

    public ProductionLine() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<ProductionLog> getProductionLogs() {
        return productionLogs;
    }

    public void setProductionLogs(List<ProductionLog> productionLogs) {
        this.productionLogs = productionLogs;
    }
}
