package com.steelworks.repository;

import com.steelworks.model.ProductionLine;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ProductionLine entity. AC4, AC5: Used to attribute defects to lines and rank them.
 */
@Repository
public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {

    Optional<ProductionLine> findByLineName(String lineName);
}
