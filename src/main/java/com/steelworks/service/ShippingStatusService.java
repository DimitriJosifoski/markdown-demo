package com.steelworks.service;

import com.steelworks.dto.ShippingRiskAlertDTO;
import com.steelworks.enums.ShipStatus;
import com.steelworks.model.ProductionLog;
import com.steelworks.model.ShippingLog;
import com.steelworks.repository.ProductionLogRepository;
import com.steelworks.repository.ShippingLogRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Service for shipping status determination and risk analysis. AC3: Determines whether a lot is
 * "Shipped" or "In Inventory." AC6: Identifies problematic shipped batches (lots with critical
 * defects that shipped).
 */
@Service
public class ShippingStatusService {

    private final ShippingLogRepository shippingLogRepository;
    private final ProductionLogRepository productionLogRepository;

    public ShippingStatusService(ShippingLogRepository shippingLogRepository,
            ProductionLogRepository productionLogRepository) {
        this.shippingLogRepository = shippingLogRepository;
        this.productionLogRepository = productionLogRepository;
    }

    /**
     * Determines the shipping status for a given lot. AC3: "Shipped" only if the Lot ID appears in
     * Shipping log with a valid ship date; otherwise "In Inventory."
     *
     * @param lotId
     *            the database ID of the lot
     * @return SHIPPED or IN_INVENTORY
     */
    public ShipStatus determineShippingStatus(Long lotId) {
        boolean hasShippingRecord = shippingLogRepository.existsByLotIdAndShipDateIsNotNull(lotId);
        return hasShippingRecord ? ShipStatus.SHIPPED : ShipStatus.IN_INVENTORY;
    }

    /**
     * Returns a high-priority list of lots with critical defects that have shipped. AC6:
     * "Problematic Shipped Batches" — lots with critical defects and an associated ship date.
     *
     * @return list of shipping risk alerts ordered by severity
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public List<ShippingRiskAlertDTO> getProblematicShippedBatches() {
        List<ProductionLog> criticalProductionLogs = productionLogRepository.findByIssueFlagTrue()
                .stream().filter(log -> log.getDefectType() != null)
                .filter(log -> "CRITICAL".equalsIgnoreCase(log.getDefectType().getSeverity()))
                .toList();

        if (criticalProductionLogs.isEmpty()) {
            return List.of();
        }

        List<Long> lotIds = criticalProductionLogs.stream().map(log -> log.getLot().getId())
                .distinct().toList();
        List<ShippingLog> shippedLogs = shippingLogRepository.findShippedLogsForLotIds(lotIds);

        Map<String, ShippingRiskAlertDTO> alertsByKey = new LinkedHashMap<>();
        for (ProductionLog productionLog : criticalProductionLogs) {
            ShippingLog shippingLog = findLatestShippingForLot(shippedLogs,
                    productionLog.getLot().getId());
            if (shippingLog == null) {
                continue;
            }

            String dedupeKey = productionLog.getLot().getId() + "|"
                    + productionLog.getDefectType().getDefectName();
            alertsByKey.putIfAbsent(dedupeKey, toAlert(productionLog, shippingLog));
        }

        List<ShippingRiskAlertDTO> alerts = new ArrayList<>(alertsByKey.values());
        alerts.sort(Comparator.comparing(ShippingRiskAlertDTO::getShipDate).reversed());
        return alerts;
    }

    private ShippingLog findLatestShippingForLot(List<ShippingLog> shippedLogs, Long lotId) {
        return shippedLogs.stream().filter(log -> log.getLot().getId().equals(lotId))
                .max(Comparator.comparing(ShippingLog::getShipDate)).orElse(null);
    }

    private ShippingRiskAlertDTO toAlert(ProductionLog productionLog, ShippingLog shippingLog) {
        ShippingRiskAlertDTO alert = new ShippingRiskAlertDTO();
        alert.setLotIdentifier(productionLog.getLot().getLotIdentifier());
        alert.setDefectName(productionLog.getDefectType().getDefectName());
        alert.setDefectSeverity(productionLog.getDefectType().getSeverity());
        alert.setShipDate(shippingLog.getShipDate());
        alert.setCustomerName(shippingLog.getCustomer().getCustomerName());
        alert.setProductionLineName(productionLog.getProductionLine().getLineName());
        return alert;
    }
}
