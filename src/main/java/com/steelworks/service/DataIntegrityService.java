package com.steelworks.service;

import com.steelworks.dto.DataConflictDTO;
import com.steelworks.repository.LotRepository;
import com.steelworks.repository.ProductionLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for data integrity checks and source traceability.
 * AC9:  Provides source file name/location for any data point.
 * AC11: Detects conflicts where the same Lot ID maps to multiple Production Lines.
 */
@Service
public class DataIntegrityService {

    private final LotRepository lotRepository;
    private final ProductionLogRepository productionLogRepository;

    public DataIntegrityService(LotRepository lotRepository,
                                ProductionLogRepository productionLogRepository) {
        this.lotRepository = lotRepository;
        this.productionLogRepository = productionLogRepository;
    }

    /**
     * Retrieves the source file name/location for a specific data point.
     * AC9: Users must be able to see the source file for any data point
     *      in a consolidated view.
     *
     * @param entityType the type of entity ("production", "shipping", "quality")
     * @param recordId   the database ID of the record
     * @return the source file path or reference string
     */
    public String getSourceReference(String entityType, Long recordId) {
        // TODO: Look up and return the source file/location for the given record
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Detects Lot IDs that are associated with multiple Production Lines across files.
     * AC11: Flags "Data Conflict" entries for manual review.
     *
     * @return list of data conflicts with the conflicting production line details
     */
    public List<DataConflictDTO> detectDataConflicts() {
        // TODO: Use productionLogRepository.findLotIdsWithMultipleProductionLines()
        //       and build DataConflictDTOs with conflicting line details
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
