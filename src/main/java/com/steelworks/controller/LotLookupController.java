package com.steelworks.controller;

import com.steelworks.dto.ConsolidatedLotView;
import com.steelworks.dto.DataConflictDTO;
import com.steelworks.dto.LotSearchRequest;
import com.steelworks.dto.LotSearchResult;
import com.steelworks.dto.OrphanedRecordDTO;
import com.steelworks.service.DataIntegrityService;
import com.steelworks.service.LotLookupService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for lot lookup operations.
 * Supports searching by Lot ID (with fuzzy matching) and date range,
 * and provides data integrity endpoints.
 */
@RestController
@RequestMapping("/api/lots")
public class LotLookupController {

    private final LotLookupService lotLookupService;
    private final DataIntegrityService dataIntegrityService;

    public LotLookupController(LotLookupService lotLookupService,
                               DataIntegrityService dataIntegrityService) {
        this.lotLookupService = lotLookupService;
        this.dataIntegrityService = dataIntegrityService;
    }

    /**
     * Searches lots by ID and/or date range.
     * AC1: Returns cross-referenced data from all three sources.
     * AC2: Supports fuzzy matching on Lot ID.
     *
     * @param lotId     optional Lot ID (supports fuzzy input)
     * @param startDate optional start of date range
     * @param endDate   optional end of date range
     * @return list of matching lot results
     */
    @GetMapping("/search")
    public ResponseEntity<List<LotSearchResult>> searchLots(
            @RequestParam(required = false) String lotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // TODO: Build LotSearchRequest from params, delegate to service
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a consolidated view for a single lot joining all data sources.
     * AC1: Cross-references Production, Quality, and Shipping data.
     * AC9: Includes source file references for traceability.
     *
     * @param id the database ID of the lot
     * @return consolidated lot view
     */
    @GetMapping("/{id}/consolidated")
    public ResponseEntity<ConsolidatedLotView> getConsolidatedView(@PathVariable Long id) {
        // TODO: Delegate to LotLookupService.getConsolidatedView()
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns all orphaned records (lots missing from one or more data sources).
     * AC10: Flags unmatched records as "Orphaned Data."
     *
     * @return list of orphaned record details
     */
    @GetMapping("/orphaned")
    public ResponseEntity<List<OrphanedRecordDTO>> getOrphanedRecords() {
        // TODO: Delegate to LotLookupService.findOrphanedRecords()
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns all detected data conflicts for manual review.
     * AC11: Lot IDs associated with multiple Production Lines are flagged.
     *
     * @return list of data conflict details
     */
    @GetMapping("/conflicts")
    public ResponseEntity<List<DataConflictDTO>> getDataConflicts() {
        // TODO: Delegate to DataIntegrityService.detectDataConflicts()
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
