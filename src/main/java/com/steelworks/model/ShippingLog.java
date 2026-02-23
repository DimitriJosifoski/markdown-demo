package com.steelworks.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Record of fulfillment and logistics. AC1: One of the three data sources joined by Lot ID. AC3:
 * Presence of a valid ship date determines "Shipped" vs "In Inventory" status. AC6: Used to
 * identify "Problematic Shipped Batches."
 */
@Entity
@Table(name = "shipping_logs")
public class ShippingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ship_date", nullable = false)
    private LocalDate shipDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "sales_order_number", nullable = false)
    private String salesOrderNumber;

    @Column(name = "destination_state", nullable = false, length = 2)
    private String destinationState;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "bol_number", nullable = false, unique = true)
    private String bolNumber;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "qty_shipped", nullable = false)
    private Integer qtyShipped;

    @Column(name = "ship_status", nullable = false)
    private String shipStatus;

    @Column(name = "hold_reason")
    private String holdReason;

    @Column(name = "shipping_notes")
    private String shippingNotes;

    public ShippingLog() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }
    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public Lot getLot() {
        return lot;
    }
    public void setLot(Lot lot) {
        this.lot = lot;
    }

    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getSalesOrderNumber() {
        return salesOrderNumber;
    }
    public void setSalesOrderNumber(String salesOrderNumber) {
        this.salesOrderNumber = salesOrderNumber;
    }

    public String getDestinationState() {
        return destinationState;
    }
    public void setDestinationState(String destinationState) {
        this.destinationState = destinationState;
    }

    public String getCarrier() {
        return carrier;
    }
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getBolNumber() {
        return bolNumber;
    }
    public void setBolNumber(String bolNumber) {
        this.bolNumber = bolNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Integer getQtyShipped() {
        return qtyShipped;
    }
    public void setQtyShipped(Integer qtyShipped) {
        this.qtyShipped = qtyShipped;
    }

    public String getShipStatus() {
        return shipStatus;
    }
    public void setShipStatus(String shipStatus) {
        this.shipStatus = shipStatus;
    }

    public String getHoldReason() {
        return holdReason;
    }
    public void setHoldReason(String holdReason) {
        this.holdReason = holdReason;
    }

    public String getShippingNotes() {
        return shippingNotes;
    }
    public void setShippingNotes(String shippingNotes) {
        this.shippingNotes = shippingNotes;
    }
}
