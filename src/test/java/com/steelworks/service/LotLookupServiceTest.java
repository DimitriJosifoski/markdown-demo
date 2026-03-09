package com.steelworks.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.steelworks.dto.ConsolidatedLotView;
import com.steelworks.dto.LotSearchRequest;
import com.steelworks.dto.LotSearchResult;
import com.steelworks.dto.OrphanedRecordDTO;
import com.steelworks.enums.ShipStatus;
import com.steelworks.model.Customer;
import com.steelworks.model.DefectType;
import com.steelworks.model.Lot;
import com.steelworks.model.ProductionLine;
import com.steelworks.model.ProductionLog;
import com.steelworks.model.ShippingLog;
import com.steelworks.repository.LotRepository;
import com.steelworks.repository.ProductionLogRepository;
import com.steelworks.repository.ShippingLogRepository;
import com.steelworks.util.LotIdNormalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for LotLookupService. AC1: Cross-referencing data sources. AC2: Fuzzy matching. AC9:
 * Source transparency. AC10: Orphaned record handling.
 */
@ExtendWith(MockitoExtension.class)
class LotLookupServiceTest {

    private static final String LOT_100 = "LOT-100";
    private static final String PN_100 = "PN-100";
    private static final String LINE_A = "Line-A";
    private static final String DEFECT_CRACK = "Crack";

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
        Lot lot = createLot(1L, LOT_100, PN_100, LocalDate.of(2026, 2, 20));
        ProductionLog productionLog = createProductionLog(LINE_A, DEFECT_CRACK, "critical", true,
                100, 95, 10);
        ShippingLog shippingLog = createShippingLog(LocalDate.of(2026, 2, 22), "Acme");
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(List.of(productionLog));
        when(shippingLogRepository.findByLotId(1L)).thenReturn(List.of(shippingLog));

        List<LotSearchResult> results = lotLookupService.searchLots(new LotSearchRequest());
        LotSearchResult result = results.isEmpty() ? null : results.get(0);

        boolean hasExpectedCrossReference = results.size() == 1 && result != null
                && LOT_100.equals(result.getLotIdentifier())
                && LINE_A.equals(result.getProductionLineName())
                && ShipStatus.SHIPPED == result.getShippingStatus()
                && DEFECT_CRACK.equals(result.getDefectName())
                && "CRITICAL".equals(result.getDefectSeverity()) && !result.isHasDataConflict()
                && "db:lots/1".equals(result.getSourceReference());
        assertTrue(hasExpectedCrossReference,
                "Expected search to return one cross-referenced result with production, defect, and shipping fields");
    }

    @Test
    void searchLots_shouldUseFuzzyMatchingOnLotId() {
        Lot lot = createLot(1L, LOT_100, PN_100, LocalDate.of(2026, 2, 20));
        LotSearchRequest request = new LotSearchRequest();
        request.setLotId("lot100");
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(lotIdNormalizer.normalize("lot100")).thenReturn("LOT100");
        when(lotIdNormalizer.normalize(LOT_100)).thenReturn("NON_MATCHING");
        when(lotIdNormalizer.areEquivalent(LOT_100, "LOT100")).thenReturn(true);
        when(productionLogRepository.findByLotId(1L)).thenReturn(List.of());
        when(shippingLogRepository.findByLotId(1L)).thenReturn(List.of());

        List<LotSearchResult> results = lotLookupService.searchLots(request);
        verify(lotIdNormalizer).areEquivalent(LOT_100, "LOT100");

        boolean fuzzyMatchingWorked = results.size() == 1;
        assertTrue(fuzzyMatchingWorked,
                "Expected fuzzy search to include lot when LotIdNormalizer reports equivalence");
    }

    @Test
    void searchLots_shouldFilterByDateRange() {
        Lot lot = createLot(1L, LOT_100, PN_100, LocalDate.of(2026, 1, 1));
        LotSearchRequest request = new LotSearchRequest();
        request.setStartDate(LocalDate.of(2026, 2, 1));
        request.setEndDate(LocalDate.of(2026, 3, 1));
        when(lotRepository.findAll()).thenReturn(List.of(lot));

        List<LotSearchResult> results = lotLookupService.searchLots(request);
        verify(productionLogRepository, never()).findByLotId(anyLong());
        verify(shippingLogRepository, never()).findByLotId(anyLong());

        boolean dateFilterApplied = results.isEmpty();
        assertTrue(dateFilterApplied,
                "Expected search to exclude lots created before start date and skip downstream lookups");
    }

    @Test
    void getConsolidatedView_shouldJoinAllThreeDataSources() {
        Lot lot = createLot(1L, LOT_100, PN_100, LocalDate.of(2026, 2, 20));
        ProductionLog lineAProduction = createProductionLog(LINE_A, DEFECT_CRACK, "Critical", true,
                100, 95, 10);
        ProductionLog lineBProduction = createProductionLog("Line-B", "Pit", "Major", false, 80, 78,
                5);
        ShippingLog olderShipping = createShippingLog(LocalDate.of(2026, 2, 21), "Acme");
        ShippingLog latestShipping = createShippingLog(LocalDate.of(2026, 2, 23), "Globex");
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(1L))
                .thenReturn(List.of(lineAProduction, lineBProduction));
        when(shippingLogRepository.findByLotId(1L))
                .thenReturn(List.of(olderShipping, latestShipping));

        ConsolidatedLotView view = lotLookupService.getConsolidatedView(1L);

        boolean viewIsConsolidated = LOT_100.equals(view.getLotIdentifier())
                && PN_100.equals(view.getPartNumber())
                && List.of(LINE_A, "Line-B").equals(view.getAssociatedProductionLines())
                && Integer.valueOf(180).equals(view.getTotalUnitsPlanned())
                && Integer.valueOf(173).equals(view.getTotalUnitsActual())
                && Integer.valueOf(15).equals(view.getTotalDowntimeMinutes())
                && List.of(DEFECT_CRACK, "Pit").equals(view.getDefectsFound())
                && view.isHasIssueFlag() && "Shipped".equals(view.getShippingStatus())
                && LocalDate.of(2026, 2, 23).equals(view.getShipDate())
                && "Globex".equals(view.getCustomerName());
        assertTrue(viewIsConsolidated,
                "Expected consolidated view to merge production totals, defect list, and latest shipping details");
    }

    @Test
    void getConsolidatedView_shouldIncludeSourceReferences() {
        Lot lot = createLot(2L, "LOT-200", "PN-200", LocalDate.of(2026, 2, 20));
        when(lotRepository.findById(2L)).thenReturn(Optional.of(lot));
        when(productionLogRepository.findByLotId(2L)).thenReturn(List.of());
        when(shippingLogRepository.findByLotId(2L)).thenReturn(List.of());

        ConsolidatedLotView view = lotLookupService.getConsolidatedView(2L);

        boolean hasExpectedSources = "In Inventory".equals(view.getShippingStatus())
                && "db:production_logs".equals(view.getProductionSourceFile())
                && "db:shipping_logs".equals(view.getShippingSourceFile())
                && "db:production_logs#quality".equals(view.getQualitySourceFile());
        assertTrue(hasExpectedSources,
                "Expected consolidated view to include fixed production/shipping/quality source references");
    }

    @Test
    void findOrphanedRecords_shouldFlagLotsWithMissingSources() {
        Lot completeLot = createLot(1L, LOT_100, PN_100, LocalDate.of(2026, 2, 20));
        Lot orphanedLot = createLot(2L, "LOT-200", "PN-200", LocalDate.of(2026, 2, 20));
        ProductionLog completeProduction = createProductionLog(LINE_A, DEFECT_CRACK, "Major", true,
                100, 100, 0);
        ProductionLog orphanedProduction = createProductionLog(LINE_A, null, null, false, 100, 100,
                0);
        when(lotRepository.findAll()).thenReturn(List.of(completeLot, orphanedLot));
        when(productionLogRepository.findByLotId(1L)).thenReturn(List.of(completeProduction));
        when(shippingLogRepository.findByLotId(1L))
                .thenReturn(List.of(createShippingLog(LocalDate.of(2026, 2, 25), "Acme")));
        when(productionLogRepository.findByLotId(2L)).thenReturn(List.of(orphanedProduction));
        when(shippingLogRepository.findByLotId(2L)).thenReturn(List.of());

        List<OrphanedRecordDTO> orphanedRecords = lotLookupService.findOrphanedRecords();
        OrphanedRecordDTO orphanedRecord = orphanedRecords.isEmpty()
                ? null
                : orphanedRecords.get(0);

        boolean orphanIsFlagged = orphanedRecords.size() == 1 && orphanedRecord != null
                && "LOT-200".equals(orphanedRecord.getLotIdentifier())
                && orphanedRecord.isInProduction() && !orphanedRecord.isInShipping()
                && !orphanedRecord.isInQuality()
                && "Missing in shipping, quality.".equals(orphanedRecord.getReason());
        assertTrue(orphanIsFlagged,
                "Expected record missing shipping and quality signals to be returned as orphaned");
    }

    @Test
    void findOrphanedRecords_shouldNotExcludeUnmatchedRecords() {
        Lot lot = createLot(3L, "LOT-300", "PN-300", LocalDate.of(2026, 2, 20));
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(productionLogRepository.findByLotId(3L)).thenReturn(List.of());
        when(shippingLogRepository.findByLotId(3L)).thenReturn(List.of());

        List<OrphanedRecordDTO> orphanedRecords = lotLookupService.findOrphanedRecords();
        OrphanedRecordDTO orphanedRecord = orphanedRecords.isEmpty()
                ? null
                : orphanedRecords.get(0);

        boolean unmatchedRecordReturned = orphanedRecords.size() == 1 && orphanedRecord != null
                && "LOT-300".equals(orphanedRecord.getLotIdentifier())
                && !orphanedRecord.isInProduction() && !orphanedRecord.isInShipping()
                && !orphanedRecord.isInQuality()
                && "Missing in production, shipping, quality.".equals(orphanedRecord.getReason());
        assertTrue(unmatchedRecordReturned,
                "Expected fully unmatched lot to be returned instead of being silently excluded");
    }

    @Test
    void getConsolidatedView_shouldLogWarningAndThrowWhenLotIsMissing() {
        Logger logger = (Logger) LoggerFactory.getLogger(LotLookupService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        when(lotRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            boolean threwExpectedException = throwsLotNotFoundFor(99L);
            boolean warningLogged = listAppender.list.stream()
                    .anyMatch(event -> event.getLevel() == Level.WARN
                            && event.getFormattedMessage().contains("lotId=99"));
            assertTrue(threwExpectedException && warningLogged,
                    "Expected missing lot to throw IllegalArgumentException and emit WARN log entry");
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    private boolean throwsLotNotFoundFor(long lotId) {
        try {
            lotLookupService.getConsolidatedView(lotId);
            return false;
        } catch (IllegalArgumentException exception) {
            return ("Lot not found: " + lotId).equals(exception.getMessage());
        }
    }

    private Lot createLot(Long id, String lotIdentifier, String partNumber, LocalDate createdDate) {
        Lot lot = new Lot();
        lot.setId(id);
        lot.setLotIdentifier(lotIdentifier);
        lot.setPartNumber(partNumber);
        lot.setCreatedDate(createdDate);
        return lot;
    }

    private ProductionLog createProductionLog(String lineName, String defectName, String severity,
            boolean issueFlag, int unitsPlanned, int unitsActual, int downtimeMinutes) {
        ProductionLog productionLog = new ProductionLog();
        ProductionLine productionLine = new ProductionLine();
        productionLine.setLineName(lineName);
        productionLog.setProductionLine(productionLine);
        if (defectName != null) {
            DefectType defectType = new DefectType();
            defectType.setDefectName(defectName);
            defectType.setSeverity(severity);
            productionLog.setDefectType(defectType);
        }
        productionLog.setIssueFlag(issueFlag);
        productionLog.setUnitsPlanned(unitsPlanned);
        productionLog.setUnitsActual(unitsActual);
        productionLog.setDowntimeMinutes(downtimeMinutes);
        return productionLog;
    }

    private ShippingLog createShippingLog(LocalDate shipDate, String customerName) {
        ShippingLog shippingLog = new ShippingLog();
        Customer customer = new Customer();
        customer.setCustomerName(customerName);
        shippingLog.setCustomer(customer);
        shippingLog.setShipDate(shipDate);
        return shippingLog;
    }
}
