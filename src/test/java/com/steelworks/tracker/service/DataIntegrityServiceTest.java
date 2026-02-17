package com.steelworks.tracker.service;

import com.steelworks.tracker.dto.DataConflictDTO;
import com.steelworks.tracker.dto.OrphanedLotDTO;
import com.steelworks.tracker.model.Lot;
import com.steelworks.tracker.repository.LotRepository;
import com.steelworks.tracker.repository.ProductionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * DataIntegrityServiceTest — Unit tests for {@link DataIntegrityService}.
 *
 * <h3>AC Coverage:</h3>
 * <ul>
 *   <li><b>AC10</b> — Orphaned Data: verifies detection logic and "Orphaned Data" status.</li>
 *   <li><b>AC11</b> — Consistency Check: verifies multi-line conflict detection.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class DataIntegrityServiceTest {

    @Mock private LotRepository lotRepository;
    @Mock private ProductionLogRepository productionLogRepository;

    private DataIntegrityService dataIntegrityService;

    @BeforeEach
    void setUp() {
        dataIntegrityService = new DataIntegrityService(lotRepository, productionLogRepository);
    }

    // ════════════════════════════════════════════════════════════════════
    // AC10: Orphaned Data
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC10: findOrphanedLots returns lots without any references")
    void findOrphanedLots_returnsOrphans() {
        Lot orphan = new Lot();
        orphan.setId(10L);
        orphan.setLotIdentifier("LOT-LONELY");
        orphan.setPartNumber("SKU-X");
        orphan.setCreatedDate(LocalDate.now());

        when(lotRepository.findOrphanedLots()).thenReturn(List.of(orphan));

        List<OrphanedLotDTO> result = dataIntegrityService.findOrphanedLots();

        assertEquals(1, result.size());
        assertEquals("LOT-LONELY", result.get(0).lotIdentifier());
        assertEquals("Orphaned Data", result.get(0).status());
    }

    @Test
    @DisplayName("AC10: hasOrphanedData returns true when orphans exist")
    void hasOrphanedData_true() {
        Lot orphan = new Lot();
        orphan.setId(10L);
        orphan.setLotIdentifier("LOT-LONELY");
        orphan.setPartNumber("SKU-X");
        orphan.setCreatedDate(LocalDate.now());

        when(lotRepository.findOrphanedLots()).thenReturn(List.of(orphan));

        assertTrue(dataIntegrityService.hasOrphanedData());
    }

    @Test
    @DisplayName("AC10: hasOrphanedData returns false when no orphans")
    void hasOrphanedData_false() {
        when(lotRepository.findOrphanedLots()).thenReturn(Collections.emptyList());

        assertFalse(dataIntegrityService.hasOrphanedData());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC11: Data Conflicts
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC11: findDataConflicts returns lots on multiple lines")
    void findDataConflicts_returnsConflicts() {
        when(productionLogRepository.findLotsWithMultipleLines())
                .thenReturn(Collections.singletonList(new Object[]{"LOT-MULTI", 3L}));

        List<DataConflictDTO> result = dataIntegrityService.findDataConflicts();

        assertEquals(1, result.size());
        assertEquals("LOT-MULTI", result.get(0).lotIdentifier());
        assertEquals(3, result.get(0).distinctLineCount());
    }

    @Test
    @DisplayName("AC11: hasDataConflicts returns false when no conflicts")
    void hasDataConflicts_false() {
        when(productionLogRepository.findLotsWithMultipleLines())
                .thenReturn(Collections.emptyList());

        assertFalse(dataIntegrityService.hasDataConflicts());
    }
}
