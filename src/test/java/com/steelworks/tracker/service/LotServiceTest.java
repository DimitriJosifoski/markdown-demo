package com.steelworks.tracker.service;

import com.steelworks.tracker.dto.LotDetailDTO;
import com.steelworks.tracker.model.*;
import com.steelworks.tracker.repository.LotRepository;
import com.steelworks.tracker.repository.ProductionLogRepository;
import com.steelworks.tracker.repository.ShippingLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * LotServiceTest — Unit tests for {@link LotService}.
 *
 * <p>Uses Mockito to mock repository dependencies so we test only the
 * service logic without hitting a database.</p>
 *
 * <h3>AC Coverage:</h3>
 * <ul>
 *   <li><b>AC1</b>  — Cross-referencing: verifies that lot detail joins production + shipping data.</li>
 *   <li><b>AC2</b>  — Fuzzy Matching: verifies lookups via normalised IDs.</li>
 *   <li><b>AC3</b>  — Shipping Status: verifies "Shipped" vs "In Inventory" derivation.</li>
 *   <li><b>AC4</b>  — Line Attribution: verifies production line is attached to lot details.</li>
 *   <li><b>AC9</b>  — Source Transparency: verifies source file/row propagation.</li>
 *   <li><b>AC11</b> — Consistency Check: verifies multi-line conflict flagging.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class) // Activates Mockito annotation processing.
class LotServiceTest {

    // @Mock creates a Mockito mock (a fake implementation that we control).
    @Mock private LotRepository lotRepository;
    @Mock private ProductionLogRepository productionLogRepository;
    @Mock private ShippingLogRepository shippingLogRepository;

    // FuzzyMatchService is a real instance (it has no dependencies of its own).
    private final FuzzyMatchService fuzzyMatchService = new FuzzyMatchService();

    // @InjectMocks creates the class under test and injects the mocks above.
    // We'll construct manually since FuzzyMatchService isn't a mock.
    private LotService lotService;

    /** Set up the service before each test. */
    @BeforeEach
    void setUp() {
        lotService = new LotService(
                lotRepository,
                productionLogRepository,
                shippingLogRepository,
                fuzzyMatchService
        );
    }

    // ════════════════════════════════════════════════════════════════════
    // Shared test data builders
    // ════════════════════════════════════════════════════════════════════

    /** Create a test Lot entity. */
    private Lot createTestLot() {
        Lot lot = new Lot();
        lot.setId(1L);
        lot.setLotIdentifier("LOT-20260112-001");
        lot.setNormalizedLotId("LOT20260112001");
        lot.setPartNumber("SKU-100");
        lot.setCreatedDate(LocalDate.of(2026, 1, 12));
        return lot;
    }

    /** Create a test ProductionLog with a production line and optional defect. */
    private ProductionLog createTestProdLog(Lot lot, String lineName, DefectType defect) {
        ProductionLine line = new ProductionLine();
        line.setId(1L);
        line.setLineName(lineName);
        line.setDepartment("Assembly");

        ProductionLog log = new ProductionLog();
        log.setId(1L);
        log.setProductionDate(LocalDate.of(2026, 1, 12));
        log.setShift("Day");
        log.setUnitsPlanned(100);
        log.setUnitsActual(95);
        log.setDowntimeMinutes(10);
        log.setIssueFlag(defect != null);
        log.setLot(lot);
        log.setProductionLine(line);
        log.setDefectType(defect);
        log.setSourceFile("production_jan.csv");
        log.setSourceRowNumber(42);
        return log;
    }

    /** Create a test DefectType. */
    private DefectType createTestDefect() {
        DefectType dt = new DefectType();
        dt.setId(1L);
        dt.setDefectCode("SFC-001");
        dt.setDefectName("Surface Crack");
        dt.setSeverity("Critical");
        return dt;
    }

    // ════════════════════════════════════════════════════════════════════
    // AC2: Fuzzy lookup
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC2: findByFuzzyId resolves LOT-20260112-001 from 'LOT 20260112 001'")
    void findByFuzzyId_matchesWithSpaces() {
        Lot lot = createTestLot();
        // When the normalised form is queried, return our test lot.
        when(lotRepository.findByNormalizedLotId("LOT20260112001")).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(Collections.emptyList());
        when(shippingLogRepository.existsByLotIdAndShipStatus(1L, "Shipped")).thenReturn(false);
        when(shippingLogRepository.findByLotId(1L)).thenReturn(Collections.emptyList());

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("LOT 20260112 001");

        assertTrue(result.isPresent(), "Fuzzy match should find the lot");
        assertEquals("LOT-20260112-001", result.get().lotIdentifier());
    }

    @Test
    @DisplayName("AC2: findByFuzzyId returns empty when no match exists")
    void findByFuzzyId_noMatch() {
        when(lotRepository.findByNormalizedLotId("NONEXISTENT")).thenReturn(Optional.empty());

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("NON-EXISTENT");

        assertTrue(result.isEmpty());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC3: Shipping Status Logic
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC3: Lot with shipped record shows 'Shipped' status")
    void shippingStatus_shipped() {
        Lot lot = createTestLot();
        when(lotRepository.findByNormalizedLotId("LOT20260112001")).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(Collections.emptyList());
        // Simulate that this lot has been shipped.
        when(shippingLogRepository.existsByLotIdAndShipStatus(1L, "Shipped")).thenReturn(true);

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("LOT-20260112-001");

        assertTrue(result.isPresent());
        assertEquals("Shipped", result.get().shippingStatus());
    }

    @Test
    @DisplayName("AC3: Lot without shipped record shows 'In Inventory' status")
    void shippingStatus_inInventory() {
        Lot lot = createTestLot();
        when(lotRepository.findByNormalizedLotId("LOT20260112001")).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(Collections.emptyList());
        when(shippingLogRepository.existsByLotIdAndShipStatus(1L, "Shipped")).thenReturn(false);
        when(shippingLogRepository.findByLotId(1L)).thenReturn(Collections.emptyList());

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("LOT-20260112-001");

        assertTrue(result.isPresent());
        assertEquals("In Inventory", result.get().shippingStatus());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC1 + AC4: Cross-referencing & Line Attribution
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC1 + AC4: Lot detail includes production line name")
    void lotDetail_includesProductionLine() {
        Lot lot = createTestLot();
        ProductionLog prodLog = createTestProdLog(lot, "Line 1", null);

        when(lotRepository.findByNormalizedLotId("LOT20260112001")).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(List.of(prodLog));
        when(shippingLogRepository.existsByLotIdAndShipStatus(1L, "Shipped")).thenReturn(false);

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("LOT-20260112-001");

        assertTrue(result.isPresent());
        assertEquals("Line 1", result.get().productionLine());
    }

    @Test
    @DisplayName("AC1: Lot detail includes defect summary from production data")
    void lotDetail_includesDefectSummary() {
        Lot lot = createTestLot();
        DefectType defect = createTestDefect();
        ProductionLog prodLog = createTestProdLog(lot, "Line 1", defect);

        when(lotRepository.findByNormalizedLotId("LOT20260112001")).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(List.of(prodLog));
        when(shippingLogRepository.existsByLotIdAndShipStatus(1L, "Shipped")).thenReturn(false);

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("LOT-20260112-001");

        assertTrue(result.isPresent());
        assertEquals("Surface Crack", result.get().defectSummary());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC9: Source Transparency
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC9: Lot detail includes source file and row number")
    void lotDetail_includesSourceInfo() {
        Lot lot = createTestLot();
        ProductionLog prodLog = createTestProdLog(lot, "Line 1", null);
        prodLog.setSourceFile("production_jan.csv");
        prodLog.setSourceRowNumber(42);

        when(lotRepository.findByNormalizedLotId("LOT20260112001")).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(List.of(prodLog));
        when(shippingLogRepository.existsByLotIdAndShipStatus(1L, "Shipped")).thenReturn(false);

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("LOT-20260112-001");

        assertTrue(result.isPresent());
        assertEquals("production_jan.csv", result.get().sourceFile());
        assertEquals(42, result.get().sourceRowNumber());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC11: Consistency Check (Data Conflict in Lot Detail)
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC11: Lot on multiple production lines shows conflict in detail")
    void lotDetail_multipleLines_showsConflict() {
        Lot lot = createTestLot();
        ProductionLog log1 = createTestProdLog(lot, "Line 1", null);
        ProductionLog log2 = createTestProdLog(lot, "Line 3", null);
        // Make log2 reference a different line object.
        ProductionLine line3 = new ProductionLine();
        line3.setId(3L);
        line3.setLineName("Line 3");
        line3.setDepartment("Finishing");
        log2.setProductionLine(line3);

        when(lotRepository.findByNormalizedLotId("LOT20260112001")).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(List.of(log1, log2));
        when(shippingLogRepository.existsByLotIdAndShipStatus(1L, "Shipped")).thenReturn(false);

        Optional<LotDetailDTO> result = lotService.findByFuzzyId("LOT-20260112-001");

        assertTrue(result.isPresent());
        assertTrue(result.get().productionLine().startsWith("Multiple (Conflict)"),
                "Should flag as data conflict when lot appears on multiple lines");
    }
}
