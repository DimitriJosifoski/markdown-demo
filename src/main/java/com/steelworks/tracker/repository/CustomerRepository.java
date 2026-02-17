package com.steelworks.tracker.repository;

import com.steelworks.tracker.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CustomerRepository — Spring Data JPA repository for {@link Customer}.
 *
 * <p>Provides basic CRUD and a finder by customer name.
 * Used during data import and in the Shipping Risk Alert (AC6).</p>
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Look up a customer by their unique name (e.g., "Acme Corp").
     *
     * @param customerName the unique customer name
     * @return Optional containing the customer, or empty
     */
    Optional<Customer> findByCustomerName(String customerName);
}
