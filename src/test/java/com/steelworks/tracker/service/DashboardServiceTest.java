package com.steelworks.tracker.service;

import com.steelworks.tracker.dto.*;
import com.steelworks.tracker.model.Lot;
import com.steelworks.tracker.repository.LotRepository;
import com.steelworks.tracker.repository.ProductionLogRepository;
import com.steelworks.tracker.repository.ShippingLogRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DashboardServiceTest — Unit tests for {@link DashboardService}.
 *
 * <h3>AC Coverage:</h3>
 * <ul>
 *   <li><b>AC5</b>  — Production Line Ranking: tests defect-count ranking logic.</li>
 *   <li><b>AC6</b>  — Shipping Risk Alert: tests problematic-shipped-batch detection.</li>
 *   <li><b>AC7</b>  — Defect Trending: tests up/down/flat/new trend derivation.</li>
 *   <li><b>AC8</b>  — Default Time-Grouping: tests WEEKLY default and toggle.</li>
 *   <li><b>AC10</b> — Orphaned Data: tests orphaned lot detection.</li>
 *   <li><b>AC11</b> — Data Conflicts: tests multi-line conflict detection.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ProductionLogRepository productionLogRepository;
    @Mock private ShippingLogRepository shippingLogRepository;
    @Mock private LotRepository lotRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                productionLogRepository,
                shippingLogRepository,
                lotRepository
        );
    }

    // ════════════════════════════════════════════════════════════════════
    // AC8: Default Time-Grouping
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC8: buildDashboard defaults to WEEKLY when timeGrouping is null")
    void buildDashboard_defaultsToWeekly_whenNull() {
        // Set up empty results to avoid NPEs.
        stubEmptyRepositories();

        DashboardDTO dto = dashboardService.buildDashboard(null);

        assertEquals("WEEKLY", dto.timeGrouping(), "Should default to WEEKLY per AC8");
    }

    @Test
    @DisplayName("AC8: buildDashboard defaults to WEEKLY when timeGrouping is blank")
    void buildDashboard_defaultsToWeekly_whenBlank() {
        stubEmptyRepositories();

        DashboardDTO dto = dashboardService.buildDashboard("  ");

        assertEquals("WEEKLY", dto.timeGrouping());
    }

    @Test
    @DisplayName("AC8: buildDashboard respects DAILY toggle")
    void buildDashboard_respectsDaily() {
        stubEmptyRepositories();

        DashboardDTO dto = dashboardService.buildDashboard("DAILY");

        assertEquals("DAILY", dto.timeGrouping());
        // For DAILY, periodStart should equal periodEnd (today).
        assertEquals(dto.periodStart(), dto.periodEnd());
    }

    @Test
    @DisplayName("AC8: buildDashboard respects MONTHLY toggle")
    void buildDashboard_respectsMonthly() {
        stubEmptyRepositories();

        DashboardDTO dto = dashboardService.buildDashboard("MONTHLY");

        assertEquals("MONTHLY", dto.timeGrouping());
        // Period start should be the 1st of the current month.
        assertEquals(1, dto.periodStart().getDayOfMonth());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC5: Production Line Ranking
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC5: getLineRankings returns lines ranked by defect count descending")
    void getLineRankings_ranksCorrectly() {
        // Simulate DB returning: Line 1 → 5 defects, Line 2 → 3 defects.
        List<Object[]> dbResults = List.of(
                new Object[]{"Line 1", 5L},
                new Object[]{"Line 2", 3L}
        );
        when(productionLogRepository.countDefectsByLineInRange(any(), any()))
                .thenReturn(dbResults);

        LocalDate start = LocalDate.of(2026, 2, 9);
        LocalDate end = LocalDate.of(2026, 2, 15);
        List<LineDefectCountDTO> rankings = dashboardService.getLineRankings(start, end);

        assertEquals(2, rankings.size());
        // First entry should be the line with the most defects.
        assertEquals("Line 1", rankings.get(0).lineName());
        assertEquals(5, rankings.get(0).defectCount());
        assertEquals(1, rankings.get(0).rank());     // rank 1 = worst
        // Second entry
        assertEquals("Line 2", rankings.get(1).lineName());
        assertEquals(3, rankings.get(1).defectCount());
        assertEquals(2, rankings.get(1).rank());
    }

    @Test
    @DisplayName("AC5: getLineRankings returns empty list when no defects")
    void getLineRankings_noDefects() {
        when(productionLogRepository.countDefectsByLineInRange(any(), any()))
                .thenReturn(Collections.emptyList());

        List<LineDefectCountDTO> rankings = dashboardService.getLineRankings(
                LocalDate.now(), LocalDate.now());

        assertTrue(rankings.isEmpty());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC6: Shipping Risk Alert
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC6: getShippingRisks returns problematic shipped batches")
    void getShippingRisks_returnsRisks() {
        List<Object[]> dbResults = Collections.singletonList(
                new Object[]{"LOT-001", "Acme Corp", LocalDate.of(2026, 2, 10),
                             "Surface Crack", "Critical"}
        );
        when(shippingLogRepository.findProblematicShippedBatches()).thenReturn(dbResults);

        List<ShippingRiskDTO> risks = dashboardService.getShippingRisks();

        assertEquals(1, risks.size());
        assertEquals("LOT-001", risks.get(0).lotIdentifier());
        assertEquals("Acme Corp", risks.get(0).customerName());
        assertEquals("Surface Crack", risks.get(0).defectName());
        assertEquals("Critical", risks.get(0).severity());
    }

    @Test
    @DisplayName("AC6: getShippingRisks returns empty when no problematic batches")
    void getShippingRisks_noneFound() {
        when(shippingLogRepository.findProblematicShippedBatches())
                .thenReturn(Collections.emptyList());

        List<ShippingRiskDTO> risks = dashboardService.getShippingRisks();

        assertTrue(risks.isEmpty());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC7: Defect Trending
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC7: Defect trending detects UP trend when current > previous")
    void defectTrend_up() {
        // Current period: Surface Crack = 5 occurrences.
        List<Object[]> currentPeriod = Collections.singletonList(new Object[]{"Surface Crack", "Critical", 5L});
        // Previous period: Surface Crack = 2 occurrences.
        List<Object[]> previousPeriod = Collections.singletonList(new Object[]{"Surface Crack", "Critical", 2L});
        when(productionLogRepository.countDefectsByTypeInRange(any(), any()))
                .thenReturn(currentPeriod)
                .thenReturn(previousPeriod);

        List<DefectTrendDTO> trends = dashboardService.getDefectTrends(
                LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 15));

        assertEquals(1, trends.size());
        assertEquals("UP", trends.get(0).trendDirection());
        assertEquals(5, trends.get(0).currentCount());
        assertEquals(2, trends.get(0).previousCount());
    }

    @Test
    @DisplayName("AC7: Defect trending detects DOWN trend when current < previous")
    void defectTrend_down() {
        List<Object[]> currentPeriod = Collections.singletonList(new Object[]{"Surface Crack", "Critical", 2L});
        List<Object[]> previousPeriod = Collections.singletonList(new Object[]{"Surface Crack", "Critical", 5L});
        when(productionLogRepository.countDefectsByTypeInRange(any(), any()))
                .thenReturn(currentPeriod)
                .thenReturn(previousPeriod);

        List<DefectTrendDTO> trends = dashboardService.getDefectTrends(
                LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 15));

        assertEquals(1, trends.size());
        assertEquals("DOWN", trends.get(0).trendDirection());
    }

    @Test
    @DisplayName("AC7: Defect trending detects FLAT trend when counts are equal")
    void defectTrend_flat() {
        List<Object[]> period = Collections.singletonList(new Object[]{"Surface Crack", "Critical", 3L});
        when(productionLogRepository.countDefectsByTypeInRange(any(), any()))
                .thenReturn(period)
                .thenReturn(period);

        List<DefectTrendDTO> trends = dashboardService.getDefectTrends(
                LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 15));

        assertEquals(1, trends.size());
        assertEquals("FLAT", trends.get(0).trendDirection());
    }

    @Test
    @DisplayName("AC7: Defect trending detects NEW trend when defect didn't exist in previous period")
    void defectTrend_new() {
        List<Object[]> currentPeriod = Collections.singletonList(new Object[]{"NEW Defect", "Minor", 4L});
        when(productionLogRepository.countDefectsByTypeInRange(any(), any()))
                .thenReturn(currentPeriod)
                // Previous period: empty → defect didn't exist.
                .thenReturn(Collections.emptyList());

        List<DefectTrendDTO> trends = dashboardService.getDefectTrends(
                LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 15));

        assertEquals(1, trends.size());
        assertEquals("NEW", trends.get(0).trendDirection());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC10: Orphaned Data
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC10: getOrphanedLots returns lots with no production or shipping data")
    void getOrphanedLots_findsOrphans() {
        Lot orphan = new Lot();
        orphan.setId(99L);
        orphan.setLotIdentifier("LOT-ORPHAN-001");
        orphan.setPartNumber("SKU-GHOST");
        orphan.setCreatedDate(LocalDate.now());

        when(lotRepository.findOrphanedLots()).thenReturn(List.of(orphan));

        List<OrphanedLotDTO> result = dashboardService.getOrphanedLots();

        assertEquals(1, result.size());
        assertEquals("LOT-ORPHAN-001", result.get(0).lotIdentifier());
        assertEquals("Orphaned Data", result.get(0).status());
    }

    @Test
    @DisplayName("AC10: getOrphanedLots returns empty when all lots are matched")
    void getOrphanedLots_noneOrphaned() {
        when(lotRepository.findOrphanedLots()).thenReturn(Collections.emptyList());

        List<OrphanedLotDTO> result = dashboardService.getOrphanedLots();

        assertTrue(result.isEmpty());
    }

    // ════════════════════════════════════════════════════════════════════
    // AC11: Data Conflicts
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC11: getDataConflicts returns lots on multiple lines")
    void getDataConflicts_findsConflicts() {
        List<Object[]> dbResults = Collections.singletonList(
                new Object[]{"LOT-CONFLICT-001", 2L}
        );
        when(productionLogRepository.findLotsWithMultipleLines()).thenReturn(dbResults);

        List<DataConflictDTO> conflicts = dashboardService.getDataConflicts();

        assertEquals(1, conflicts.size());
        assertEquals("LOT-CONFLICT-001", conflicts.get(0).lotIdentifier());
        assertEquals(2, conflicts.get(0).distinctLineCount());
    }

    @Test
    @DisplayName("AC11: getDataConflicts returns empty when no conflicts exist")
    void getDataConflicts_noConflicts() {
        when(productionLogRepository.findLotsWithMultipleLines())
                .thenReturn(Collections.emptyList());

        List<DataConflictDTO> conflicts = dashboardService.getDataConflicts();

        assertTrue(conflicts.isEmpty());
    }

    // ════════════════════════════════════════════════════════════════════
    // Helper: stub all repositories to return empty results
    // ════════════════════════════════════════════════════════════════════

    private void stubEmptyRepositories() {
        when(productionLogRepository.countDefectsByLineInRange(any(), any()))
                .thenReturn(Collections.emptyList());
        when(shippingLogRepository.findProblematicShippedBatches())
                .thenReturn(Collections.emptyList());
        when(productionLogRepository.countDefectsByTypeInRange(any(), any()))
                .thenReturn(Collections.emptyList());
        when(lotRepository.findOrphanedLots()).thenReturn(Collections.emptyList());
        when(productionLogRepository.findLotsWithMultipleLines())
                .thenReturn(Collections.emptyList());
    }
}
