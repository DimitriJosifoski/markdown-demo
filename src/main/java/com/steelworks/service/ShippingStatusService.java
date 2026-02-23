package com.steelworks.service;

import com.steelworks.dto.ShippingRiskAlertDTO;
import com.steelworks.enums.ShipStatus;
import com.steelworks.repository.ProductionLogRepository;
import com.steelworks.repository.ShippingLogRepository;
import java.util.List;
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
        // TODO: Check shipping log for valid ship date
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a high-priority list of lots with critical defects that have shipped. AC6:
     * "Problematic Shipped Batches" — lots with critical defects and an associated ship date.
     *
     * @return list of shipping risk alerts ordered by severity
     */
    public List<ShippingRiskAlertDTO> getProblematicShippedBatches() {
        // TODO: Join production defect data with shipping data, filter for critical defects
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
