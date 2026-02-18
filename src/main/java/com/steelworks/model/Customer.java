package com.steelworks.model;

import jakarta.persistence.*;

/**
 * Destination records to identify impact on specific clients.
 * AC6: Used in Shipping Risk Alerts to identify affected customers.
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, unique = true)
    private String customerName;

    @Column(name = "region", nullable = false)
    private String region;

    public Customer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
