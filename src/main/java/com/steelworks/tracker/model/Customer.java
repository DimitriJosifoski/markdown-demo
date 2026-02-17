package com.steelworks.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer — JPA entity mapped to the {@code customers} table.
 *
 * <p>Represents a downstream buyer / customer that receives shipped lots.
 * Used in the Shipping Risk Alert (AC6) to show which customer received
 * a problematic batch.</p>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC6 – Shipping Risk Alert: identifies affected customers.</li>
 * </ul>
 */
@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer {

    /** Auto-generated surrogate PK. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique display name of the customer (e.g., "Acme Corp").
     */
    @Column(name = "customer_name", nullable = false, unique = true, length = 150)
    private String customerName;

    /**
     * Sales or logistics territory (e.g., "Midwest", "Northeast").
     */
    @Column(nullable = false, length = 100)
    private String region;

    /**
     * All shipments destined for this customer.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingLog> shippingLogs = new ArrayList<>();
}
