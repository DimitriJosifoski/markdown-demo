package com.steelworks.repository;

import com.steelworks.model.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ProductionLog entity.
 * AC4: Maps defects to production lines.
 * AC5: Ranks production lines by defect count.
 * AC7: Supports defect frequency trending over time periods.
 */
@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {

    List<ProductionLog> findByLotId(Long lotId);

    List<ProductionLog> findByProductionDateBetween(LocalDate startDate, LocalDate endDate);

    List<ProductionLog> findByIssueFlagTrueAndProductionDateBetween(LocalDate startDate, LocalDate endDate);

    List<ProductionLog> findByProductionLineIdAndProductionDateBetween(
            Long productionLineId, LocalDate startDate, LocalDate endDate);

    /**
     * AC5: Count defects per production line within a date range for ranking.
     */
    @Query("SELECT pl.productionLine.lineName, COUNT(pl) FROM ProductionLog pl " +
           "WHERE pl.issueFlag = true AND pl.productionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY pl.productionLine.lineName ORDER BY COUNT(pl) DESC")
    List<Object[]> countDefectsByProductionLine(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * AC7: Count defect occurrences by defect type within a date range for trend analysis.
     */
    @Query("SELECT pl.defectType.defectName, COUNT(pl) FROM ProductionLog pl " +
           "WHERE pl.issueFlag = true AND pl.defectType IS NOT NULL " +
           "AND pl.productionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY pl.defectType.defectName")
    List<Object[]> countDefectsByType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * AC11: Find lots associated with multiple production lines (data conflict).
     */
    @Query("SELECT pl.lot.id FROM ProductionLog pl " +
           "GROUP BY pl.lot.id HAVING COUNT(DISTINCT pl.productionLine.id) > 1")
    List<Long> findLotIdsWithMultipleProductionLines();
}
