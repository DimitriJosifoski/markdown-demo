package com.steelworks.controller;

import com.steelworks.dto.ConsolidatedLotView;
import com.steelworks.dto.DataConflictDTO;
import com.steelworks.dto.LotSearchRequest;
import com.steelworks.dto.LotSearchResult;
import com.steelworks.dto.OrphanedRecordDTO;
import com.steelworks.service.DataIntegrityService;
import com.steelworks.service.LotLookupService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for lot lookup operations. Supports searching by Lot ID (with fuzzy matching) and
 * date range, and provides data integrity endpoints.
 */
@RestController
@RequestMapping("/api/lots")
public class LotLookupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LotLookupController.class);

    private final LotLookupService lotLookupService;
    private final DataIntegrityService dataIntegrityService;

    public LotLookupController(LotLookupService lotLookupService,
            DataIntegrityService dataIntegrityService) {
        this.lotLookupService = lotLookupService;
        this.dataIntegrityService = dataIntegrityService;
    }

    /**
     * Searches lots by ID and/or date range. AC1: Returns cross-referenced data from all three
     * sources. AC2: Supports fuzzy matching on Lot ID.
     *
     * @param lotId
     *            optional Lot ID (supports fuzzy input)
     * @param startDate
     *            optional start of date range
     * @param endDate
     *            optional end of date range
     * @return list of matching lot results
     */
    @GetMapping("/search")
    public ResponseEntity<List<LotSearchResult>> searchLots(
            @RequestParam(required = false) String lotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Lot search request received: lotId='{}', startDate={}, endDate={}", lotId,
                    startDate, endDate);
        }
        LotSearchRequest request = new LotSearchRequest();
        request.setLotId(lotId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        List<LotSearchResult> results = lotLookupService.searchLots(request);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Lot search completed with {} result(s)", results.size());
        }
        return ResponseEntity.ok(results);
    }

    /**
     * Returns a consolidated view for a single lot joining all data sources. AC1: Cross-references
     * Production, Quality, and Shipping data. AC9: Includes source file references for
     * traceability.
     *
     * @param id
     *            the database ID of the lot
     * @return consolidated lot view
     */
    @GetMapping("/{id}/consolidated")
    public ResponseEntity<ConsolidatedLotView> getConsolidatedView(@PathVariable Long id) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Consolidated lot view requested for lotId={}", id);
        }
        ConsolidatedLotView consolidatedLotView = lotLookupService.getConsolidatedView(id);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Consolidated lot view produced for lotId={} with shippingStatus={}", id,
                    consolidatedLotView.getShippingStatus());
        }
        return ResponseEntity.ok(consolidatedLotView);
    }

    /**
     * Returns all orphaned records (lots missing from one or more data sources). AC10: Flags
     * unmatched records as "Orphaned Data."
     *
     * @return list of orphaned record details
     */
    @GetMapping("/orphaned")
    public ResponseEntity<List<OrphanedRecordDTO>> getOrphanedRecords() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Orphaned records query requested");
        }
        List<OrphanedRecordDTO> orphanedRecords = lotLookupService.findOrphanedRecords();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Orphaned records query returned {} record(s)", orphanedRecords.size());
        }
        return ResponseEntity.ok(orphanedRecords);
    }

    /**
     * Returns all detected data conflicts for manual review. AC11: Lot IDs associated with multiple
     * Production Lines are flagged.
     *
     * @return list of data conflict details
     */
    @GetMapping("/conflicts")
    public ResponseEntity<List<DataConflictDTO>> getDataConflicts() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Data conflict query requested");
        }
        List<DataConflictDTO> dataConflicts = dataIntegrityService.detectDataConflicts();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Data conflict query returned {} record(s)", dataConflicts.size());
        }
        return ResponseEntity.ok(dataConflicts);
    }
}
