package com.steelworks.tracker.repository;

import com.steelworks.tracker.model.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ProductionLineRepository — Spring Data JPA repository for {@link ProductionLine}.
 *
 * <p>Provides basic CRUD plus a finder by line name.
 * No custom queries needed beyond auto-generated ones because the heavy
 * aggregation work is done in {@link ProductionLogRepository}.</p>
 */
@Repository
public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {

    /**
     * Find a production line by its human-readable name (e.g., "Line 1").
     *
     * @param lineName the unique line name
     * @return Optional containing the line, or empty
     *
     * <p><b>Time complexity:</b> O(log n) — {@code line_name} has a UNIQUE index.</p>
     */
    Optional<ProductionLine> findByLineName(String lineName);
}
