package com.steelworks.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central anchor record representing a manufacturing lot. Normalizes inconsistent IDs across
 * different team logs. Maps to the "lots" table in the database.
 */
@Entity
@Table(name = "lots")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lot_identifier", nullable = false, unique = true)
    private String lotIdentifier;

    @Column(name = "part_number", nullable = false)
    private String partNumber;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @OneToMany(mappedBy = "lot", fetch = FetchType.LAZY)
    private List<ProductionLog> productionLogs = new ArrayList<>();

    @OneToMany(mappedBy = "lot", fetch = FetchType.LAZY)
    private List<ShippingLog> shippingLogs = new ArrayList<>();

    public Lot() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLotIdentifier() {
        return lotIdentifier;
    }

    public void setLotIdentifier(String lotIdentifier) {
        this.lotIdentifier = lotIdentifier;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public List<ProductionLog> getProductionLogs() {
        return productionLogs;
    }

    public void setProductionLogs(List<ProductionLog> productionLogs) {
        this.productionLogs = productionLogs;
    }

    public List<ShippingLog> getShippingLogs() {
        return shippingLogs;
    }

    public void setShippingLogs(List<ShippingLog> shippingLogs) {
        this.shippingLogs = shippingLogs;
    }
}
