package com.steelworks.service;

import com.steelworks.dto.ConsolidatedLotView;
import com.steelworks.dto.LotSearchRequest;
import com.steelworks.dto.LotSearchResult;
import com.steelworks.dto.OrphanedRecordDTO;
import com.steelworks.enums.ShipStatus;
import com.steelworks.model.Lot;
import com.steelworks.model.ProductionLog;
import com.steelworks.model.ShippingLog;
import com.steelworks.repository.LotRepository;
import com.steelworks.repository.ProductionLogRepository;
import com.steelworks.repository.ShippingLogRepository;
import com.steelworks.util.LotIdNormalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Service for lot lookup and cross-referencing. AC1: Joins data from Quality, Shipping, and
 * Production using Lot ID. AC2: Fuzzy matching via LotIdNormalizer. AC9: Provides source
 * transparency in consolidated views. AC10: Handles orphaned / unmatched records.
 */
@Service
public class LotLookupService {

    private final LotRepository lotRepository;
    private final ProductionLogRepository productionLogRepository;
    private final ShippingLogRepository shippingLogRepository;
    private final LotIdNormalizer lotIdNormalizer;

    public LotLookupService(LotRepository lotRepository,
            ProductionLogRepository productionLogRepository,
            ShippingLogRepository shippingLogRepository, LotIdNormalizer lotIdNormalizer) {
        this.lotRepository = lotRepository;
        this.productionLogRepository = productionLogRepository;
        this.shippingLogRepository = shippingLogRepository;
        this.lotIdNormalizer = lotIdNormalizer;
    }

    /**
     * Searches for lots by ID (with fuzzy matching) and optional date range. AC1: Cross-references
     * Production, Quality (defect data), and Shipping sources. AC2: Uses fuzzy matching on Lot ID
     * input.
     *
     * @param request
     *            the search criteria (lot ID, date range)
     * @return list of matching lot results with cross-referenced data
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public List<LotSearchResult> searchLots(LotSearchRequest request) {
        String lotIdFilter = request != null ? request.getLotId() : null;
        LocalDate startDateFilter = request != null ? request.getStartDate() : null;
        LocalDate endDateFilter = request != null ? request.getEndDate() : null;

        List<LotSearchResult> results = new ArrayList<>();
        for (Lot lot : lotRepository.findAll()) {
            if (!matchesLotIdFilter(lot, lotIdFilter)) {
                continue;
            }
            if (!matchesDateFilter(lot, startDateFilter, endDateFilter)) {
                continue;
            }
            results.add(buildSearchResult(lot));
        }

        results.sort(Comparator.comparing(LotSearchResult::getLotIdentifier));
        return results;
    }

    /**
     * Builds a consolidated view for a single lot, joining all data sources. AC1: Cross-references
     * three data sources using Lot ID as primary key. AC9: Populates source file references for
     * traceability.
     *
     * @param lotId
     *            the database ID of the lot
     * @return consolidated view with production, quality, and shipping data
     */
    public ConsolidatedLotView getConsolidatedView(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found: " + lotId));

        List<ProductionLog> productionLogs = productionLogRepository.findByLotId(lotId);
        List<ShippingLog> shippingLogs = shippingLogRepository.findByLotId(lotId);

        ConsolidatedLotView view = new ConsolidatedLotView();
        view.setLotIdentifier(lot.getLotIdentifier());
        view.setPartNumber(lot.getPartNumber());
        view.setCreatedDate(lot.getCreatedDate());

        view.setAssociatedProductionLines(
                productionLogs.stream().map(ProductionLog::getProductionLine)
                        .map(line -> line.getLineName()).distinct().sorted().toList());
        view.setTotalUnitsPlanned(productionLogs.stream().map(ProductionLog::getUnitsPlanned)
                .filter(value -> value != null).mapToInt(Integer::intValue).sum());
        view.setTotalUnitsActual(productionLogs.stream().map(ProductionLog::getUnitsActual)
                .filter(value -> value != null).mapToInt(Integer::intValue).sum());
        view.setTotalDowntimeMinutes(productionLogs.stream().map(ProductionLog::getDowntimeMinutes)
                .filter(value -> value != null).mapToInt(Integer::intValue).sum());

        view.setDefectsFound(productionLogs.stream().map(ProductionLog::getDefectType)
                .filter(defect -> defect != null).map(defect -> defect.getDefectName()).distinct()
                .sorted().toList());
        view.setHasIssueFlag(
                productionLogs.stream().anyMatch(log -> Boolean.TRUE.equals(log.getIssueFlag())));

        ShippingLog latestShippingLog = shippingLogs.stream()
                .max(Comparator.comparing(ShippingLog::getShipDate)).orElse(null);
        if (latestShippingLog == null) {
            view.setShippingStatus("In Inventory");
        } else {
            view.setShippingStatus("Shipped");
            view.setShipDate(latestShippingLog.getShipDate());
            view.setCustomerName(latestShippingLog.getCustomer().getCustomerName());
        }

        view.setProductionSourceFile("db:production_logs");
        view.setShippingSourceFile("db:shipping_logs");
        view.setQualitySourceFile("db:production_logs#quality");

        return view;
    }

    /**
     * Finds lots that exist in one data source but not in others. AC10: Flags unmatched records as
     * "Orphaned Data" instead of excluding them.
     *
     * @return list of orphaned records with details on which sources are missing
     */
    public List<OrphanedRecordDTO> findOrphanedRecords() {
        List<OrphanedRecordDTO> orphanedRecords = new ArrayList<>();

        for (Lot lot : lotRepository.findAll()) {
            List<ProductionLog> productionLogs = productionLogRepository.findByLotId(lot.getId());
            List<ShippingLog> shippingLogs = shippingLogRepository.findByLotId(lot.getId());

            boolean inProduction = !productionLogs.isEmpty();
            boolean inShipping = !shippingLogs.isEmpty();
            boolean inQuality = productionLogs.stream().anyMatch(
                    log -> Boolean.TRUE.equals(log.getIssueFlag()) || log.getDefectType() != null);

            if (inProduction && inShipping && inQuality) {
                continue;
            }

            OrphanedRecordDTO dto = new OrphanedRecordDTO();
            dto.setLotIdentifier(lot.getLotIdentifier());
            dto.setInProduction(inProduction);
            dto.setInShipping(inShipping);
            dto.setInQuality(inQuality);
            dto.setReason(buildOrphanReason(inProduction, inShipping, inQuality));
            orphanedRecords.add(dto);
        }

        orphanedRecords.sort(Comparator.comparing(OrphanedRecordDTO::getLotIdentifier));
        return orphanedRecords;
    }

    private boolean matchesLotIdFilter(Lot lot, String rawQuery) {
        String normalizedQuery = lotIdNormalizer.normalize(rawQuery);
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return true;
        }
        String normalizedLotId = lotIdNormalizer.normalize(lot.getLotIdentifier());
        if (normalizedLotId == null) {
            return false;
        }
        return normalizedLotId.contains(normalizedQuery)
                || lotIdNormalizer.areEquivalent(lot.getLotIdentifier(), normalizedQuery);
    }

    private boolean matchesDateFilter(Lot lot, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && lot.getCreatedDate().isBefore(startDate)) {
            return false;
        }
        if (endDate != null && lot.getCreatedDate().isAfter(endDate)) {
            return false;
        }
        return true;
    }

    private LotSearchResult buildSearchResult(Lot lot) {
        List<ProductionLog> productionLogs = productionLogRepository.findByLotId(lot.getId());
        List<ShippingLog> shippingLogs = shippingLogRepository.findByLotId(lot.getId());

        LotSearchResult result = new LotSearchResult();
        result.setLotId(lot.getId());
        result.setLotIdentifier(lot.getLotIdentifier());
        result.setPartNumber(lot.getPartNumber());

        result.setProductionLineName(productionLogs.stream().map(ProductionLog::getProductionLine)
                .map(line -> line.getLineName()).distinct().sorted()
                .reduce((left, right) -> left + ", " + right).orElse(null));

        result.setShippingStatus(
                shippingLogs.isEmpty() ? ShipStatus.IN_INVENTORY : ShipStatus.SHIPPED);

        ProductionLog representativeDefect = productionLogs.stream()
                .filter(log -> log.getDefectType() != null).findFirst().orElse(null);
        if (representativeDefect != null) {
            result.setDefectName(representativeDefect.getDefectType().getDefectName());
            result.setDefectSeverity(
                    representativeDefect.getDefectType().getSeverity().toUpperCase(Locale.ROOT));
        }

        long uniqueLines = productionLogs.stream().map(ProductionLog::getProductionLine)
                .map(line -> line.getLineName()).distinct().count();
        result.setHasDataConflict(uniqueLines > 1);
        result.setSourceReference("db:lots/" + lot.getId());
        return result;
    }

    private String buildOrphanReason(boolean inProduction, boolean inShipping, boolean inQuality) {
        List<String> missingSources = new ArrayList<>(3);
        if (!inProduction) {
            missingSources.add("production");
        }
        if (!inShipping) {
            missingSources.add("shipping");
        }
        if (!inQuality) {
            missingSources.add("quality");
        }
        return "Missing in " + String.join(", ", missingSources) + ".";
    }
}
