package com.steelworks.repository;

import com.steelworks.model.DefectType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DefectType entity. AC7: Provides defect type data for trending analysis.
 */
@Repository
public interface DefectTypeRepository extends JpaRepository<DefectType, Long> {

    Optional<DefectType> findByDefectCode(String defectCode);
}
