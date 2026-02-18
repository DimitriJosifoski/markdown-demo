package com.steelworks.repository;

import com.steelworks.model.ShippingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ShippingLog entity.
 * AC3: Determines shipping status by presence of valid ship date.
 * AC6: Identifies problematic shipped batches.
 */
@Repository
public interface ShippingLogRepository extends JpaRepository<ShippingLog, Long> {

    List<ShippingLog> findByLotId(Long lotId);

    List<ShippingLog> findByShipDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByLotIdAndShipDateIsNotNull(Long lotId);

    /**
     * AC6: Find shipped lots that also have critical defects.
     */
    @Query("SELECT s FROM ShippingLog s " +
           "WHERE s.shipStatus = 'Shipped' " +
           "AND s.lot.id IN :lotIds")
    List<ShippingLog> findShippedLogsForLotIds(@Param("lotIds") List<Long> lotIds);
}
