package com.steelworks.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.steelworks.repository.LotRepository;
import com.steelworks.repository.ProductionLogRepository;
import com.steelworks.repository.ShippingLogRepository;
import com.steelworks.util.LotIdNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for LotLookupService. AC1: Cross-referencing data sources. AC2: Fuzzy matching. AC9:
 * Source transparency. AC10: Orphaned record handling.
 */
@ExtendWith(MockitoExtension.class)
class LotLookupServiceTest {

    private static final String TODO_MESSAGE = "TODO: add assertions";

    @Mock
    private LotRepository lotRepository;

    @Mock
    private ProductionLogRepository productionLogRepository;

    @Mock
    private ShippingLogRepository shippingLogRepository;

    @Mock
    private LotIdNormalizer lotIdNormalizer;

    @InjectMocks
    private LotLookupService lotLookupService;

    @Test
    void searchLots_shouldReturnCrossReferencedResults() {
        // TODO: AC1 - Verify results join Production, Quality, and Shipping data
        assertNotNull(lotLookupService, TODO_MESSAGE);
    }

    @Test
    void searchLots_shouldUseFuzzyMatchingOnLotId() {
        // TODO: AC2 - Verify fuzzy matching delegates to LotIdNormalizer
        assertNotNull(lotLookupService, TODO_MESSAGE);
    }

    @Test
    void searchLots_shouldFilterByDateRange() {
        // TODO: Verify date range filtering works correctly
        assertNotNull(lotLookupService, TODO_MESSAGE);
    }

    @Test
    void getConsolidatedView_shouldJoinAllThreeDataSources() {
        // TODO: AC1 - Verify consolidated view includes production, quality, and shipping
        assertNotNull(lotLookupService, TODO_MESSAGE);
    }

    @Test
    void getConsolidatedView_shouldIncludeSourceReferences() {
        // TODO: AC9 - Verify source file references are populated
        assertNotNull(lotLookupService, TODO_MESSAGE);
    }

    @Test
    void findOrphanedRecords_shouldFlagLotsWithMissingSources() {
        // TODO: AC10 - Verify lots missing from some sources are flagged as orphaned
        assertNotNull(lotLookupService, TODO_MESSAGE);
    }

    @Test
    void findOrphanedRecords_shouldNotExcludeUnmatchedRecords() {
        // TODO: AC10 - Verify unmatched records appear in results, not silently dropped
        assertNotNull(lotLookupService, TODO_MESSAGE);
    }
}
