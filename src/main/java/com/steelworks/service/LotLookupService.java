package com.steelworks.service;

import com.steelworks.dto.ConsolidatedLotView;
import com.steelworks.dto.LotSearchRequest;
import com.steelworks.dto.LotSearchResult;
import com.steelworks.dto.OrphanedRecordDTO;
import com.steelworks.repository.LotRepository;
import com.steelworks.repository.ProductionLogRepository;
import com.steelworks.repository.ShippingLogRepository;
import com.steelworks.util.LotIdNormalizer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for lot lookup and cross-referencing.
 * AC1: Joins data from Quality, Shipping, and Production using Lot ID.
 * AC2: Fuzzy matching via LotIdNormalizer.
 * AC9: Provides source transparency in consolidated views.
 * AC10: Handles orphaned / unmatched records.
 */
@Service
public class LotLookupService {

    private final LotRepository lotRepository;
    private final ProductionLogRepository productionLogRepository;
    private final ShippingLogRepository shippingLogRepository;
    private final LotIdNormalizer lotIdNormalizer;

    public LotLookupService(LotRepository lotRepository,
                            ProductionLogRepository productionLogRepository,
                            ShippingLogRepository shippingLogRepository,
                            LotIdNormalizer lotIdNormalizer) {
        this.lotRepository = lotRepository;
        this.productionLogRepository = productionLogRepository;
        this.shippingLogRepository = shippingLogRepository;
        this.lotIdNormalizer = lotIdNormalizer;
    }

    /**
     * Searches for lots by ID (with fuzzy matching) and optional date range.
     * AC1: Cross-references Production, Quality (defect data), and Shipping sources.
     * AC2: Uses fuzzy matching on Lot ID input.
     *
     * @param request the search criteria (lot ID, date range)
     * @return list of matching lot results with cross-referenced data
     */
    public List<LotSearchResult> searchLots(LotSearchRequest request) {
        // TODO: Implement lot search with fuzzy matching and date filtering
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Builds a consolidated view for a single lot, joining all data sources.
     * AC1: Cross-references three data sources using Lot ID as primary key.
     * AC9: Populates source file references for traceability.
     *
     * @param lotId the database ID of the lot
     * @return consolidated view with production, quality, and shipping data
     */
    public ConsolidatedLotView getConsolidatedView(Long lotId) {
        // TODO: Implement data consolidation across all three sources
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Finds lots that exist in one data source but not in others.
     * AC10: Flags unmatched records as "Orphaned Data" instead of excluding them.
     *
     * @return list of orphaned records with details on which sources are missing
     */
    public List<OrphanedRecordDTO> findOrphanedRecords() {
        // TODO: Identify lots missing from Production or Shipping sources
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
