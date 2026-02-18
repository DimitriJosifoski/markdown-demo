package com.steelworks.repository;

import com.steelworks.model.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Lot entity.
 * AC1: Primary key for cross-referencing three data sources.
 * AC2: Supports fuzzy lookup by lot identifier.
 * AC10: Used to detect orphaned records.
 */
@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    Optional<Lot> findByLotIdentifier(String lotIdentifier);

    List<Lot> findByLotIdentifierContainingIgnoreCase(String partialLotId);
}
