package com.steelworks.tracker.repository;

import com.steelworks.tracker.model.DefectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DefectTypeRepository — Spring Data JPA repository for {@link DefectType}.
 *
 * <p>Provides basic CRUD and a finder by defect code.
 * Used during data import to resolve the defect reference for each production log.</p>
 */
@Repository
public interface DefectTypeRepository extends JpaRepository<DefectType, Long> {

    /**
     * Look up a defect type by its short code (e.g., "CHG-DLY").
     *
     * @param defectCode the unique defect code
     * @return Optional containing the defect type, or empty
     */
    Optional<DefectType> findByDefectCode(String defectCode);
}
