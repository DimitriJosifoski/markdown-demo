package com.steelworks.tracker.service;

import com.steelworks.tracker.dto.DataConflictDTO;
import com.steelworks.tracker.dto.OrphanedLotDTO;
import com.steelworks.tracker.model.Lot;
import com.steelworks.tracker.repository.LotRepository;
import com.steelworks.tracker.repository.ProductionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DataIntegrityService — Dedicated service for data-quality checks (AC10, AC11).
 *
 * <p>While these checks also appear in {@link DashboardService} (for the
 * dashboard view), this service provides them as a standalone API so they
 * can be called independently — for example, during a data import pipeline
 * or a scheduled integrity audit.</p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>AC10 – Orphaned data detection: lots without production or shipping records.</li>
 *   <li>AC11 – Consistency checks: lots tied to multiple production lines.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class DataIntegrityService {

    private final LotRepository lotRepository;
    private final ProductionLogRepository productionLogRepository;

    public DataIntegrityService(LotRepository lotRepository,
                                ProductionLogRepository productionLogRepository) {
        this.lotRepository = lotRepository;
        this.productionLogRepository = productionLogRepository;
    }

    /**
     * AC10 — Return all lots that have no related production or shipping data.
     *
     * @return list of orphaned lot DTOs flagged as "Orphaned Data"
     *
     * <p><b>Time complexity:</b> O(n) where n = total lots.</p>
     */
    public List<OrphanedLotDTO> findOrphanedLots() {
        List<Lot> orphans = lotRepository.findOrphanedLots();
        return orphans.stream()
                .map(lot -> new OrphanedLotDTO(
                        lot.getLotIdentifier(),
                        lot.getPartNumber(),
                        "Orphaned Data"
                ))
                .collect(Collectors.toList());
    }

    /**
     * AC11 — Return all lots that appear on more than one production line.
     *
     * @return list of data conflict DTOs
     *
     * <p><b>Time complexity:</b> O(n) where n = total production logs.</p>
     */
    public List<DataConflictDTO> findDataConflicts() {
        List<Object[]> raw = productionLogRepository.findLotsWithMultipleLines();
        return raw.stream()
                .map(row -> new DataConflictDTO(
                        (String) row[0],
                        (Long) row[1]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Convenience method: returns {@code true} if there are ANY orphaned lots
     * in the system. Useful for showing a warning badge on the dashboard.
     *
     * @return true if at least one orphaned lot exists
     */
    public boolean hasOrphanedData() {
        return !lotRepository.findOrphanedLots().isEmpty();
    }

    /**
     * Convenience method: returns {@code true} if there are ANY data conflicts.
     *
     * @return true if at least one lot appears on multiple production lines
     */
    public boolean hasDataConflicts() {
        return !productionLogRepository.findLotsWithMultipleLines().isEmpty();
    }
}
