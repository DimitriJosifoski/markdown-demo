package com.steelworks.service;

import com.steelworks.dto.DataConflictDTO;
import com.steelworks.model.ProductionLog;
import com.steelworks.repository.LotRepository;
import com.steelworks.repository.ProductionLogRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Service for data integrity checks and source traceability. AC9: Provides source file
 * name/location for any data point. AC11: Detects conflicts where the same Lot ID maps to multiple
 * Production Lines.
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
     * Retrieves the source file name/location for a specific data point. AC9: Users must be able to
     * see the source file for any data point in a consolidated view.
     *
     * @param entityType
     *            the type of entity ("production", "shipping", "quality")
     * @param recordId
     *            the database ID of the record
     * @return the source file path or reference string
     */
    public String getSourceReference(String entityType, Long recordId) {
        if (entityType == null || recordId == null) {
            return null;
        }

        String normalizedType = entityType.toLowerCase(Locale.ROOT);
        return switch (normalizedType) {
            case "production" -> "db:production_logs/" + recordId;
            case "shipping" -> "db:shipping_logs/" + recordId;
            case "quality" -> "db:production_logs/" + recordId + "#quality";
            case "lot" -> "db:lots/" + recordId;
            default -> "db:unknown/" + recordId;
        };
    }

    /**
     * Detects Lot IDs that are associated with multiple Production Lines across files. AC11: Flags
     * "Data Conflict" entries for manual review.
     *
     * @return list of data conflicts with the conflicting production line details
     */
    public List<DataConflictDTO> detectDataConflicts() {
        List<Long> conflictingLotIds = productionLogRepository
                .findLotIdsWithMultipleProductionLines();
        List<DataConflictDTO> conflicts = new ArrayList<>(conflictingLotIds.size());

        for (Long lotId : conflictingLotIds) {
            List<String> lineNames = productionLogRepository.findByLotId(lotId).stream()
                    .map(ProductionLog::getProductionLine).map(line -> line.getLineName())
                    .distinct().sorted().toList();

            String lotIdentifier = lotRepository.findById(lotId).map(lot -> lot.getLotIdentifier())
                    .orElse("UNKNOWN_LOT_" + lotId);

            DataConflictDTO dto = new DataConflictDTO();
            dto.setLotIdentifier(lotIdentifier);
            dto.setConflictingProductionLines(lineNames);
            dto.setDescription(
                    "Lot is linked to multiple production lines and needs manual review.");
            conflicts.add(dto);
        }

        return conflicts;
    }
}
