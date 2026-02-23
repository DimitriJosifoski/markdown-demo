package com.steelworks.repository;

import com.steelworks.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Customer entity. AC6: Identifies customers affected by problematic shipped
 * batches.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
