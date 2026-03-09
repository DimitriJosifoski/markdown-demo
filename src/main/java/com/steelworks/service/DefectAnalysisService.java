package com.steelworks.service;

import com.steelworks.dto.DefectTrendDTO;
import com.steelworks.dto.ProductionLineRankingDTO;
import com.steelworks.model.ProductionLog;
import com.steelworks.repository.ProductionLogRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/**
 * Service for defect analysis, line attribution, ranking, and trending. AC4: Maps defects to
 * specific Production Lines based on Production log timestamps. AC5: Ranks production lines by
 * total defect count for the current period. AC7: Computes defect trend direction
 * (increasing/decreasing/stable).
 */
@Service
public class DefectAnalysisService {

    private final ProductionLogRepository productionLogRepository;

    public DefectAnalysisService(ProductionLogRepository productionLogRepository) {
        this.productionLogRepository = productionLogRepository;
    }

    /**
     * Retrieves the production line name responsible for a given defect (production log entry).
     * AC4: Every defect is mapped to a specific Production Line based on the Production log.
     *
     * @param productionLogId
     *            the ID of the production log entry with the defect
     * @return the name of the production line attributed to the defect
     */
    public String getLineAttribution(Long productionLogId) {
        return productionLogRepository.findById(productionLogId)
                .map(ProductionLog::getProductionLine).map(line -> line.getLineName()).orElse(null);
    }

    /**
     * Ranks production lines by total defect count within the given date range. AC5: Summary view
     * ranks production lines by total defect count for the current week.
     *
     * @param startDate
     *            beginning of the period
     * @param endDate
     *            end of the period
     * @return ranked list of production lines, highest defects first
     */
    public List<ProductionLineRankingDTO> rankProductionLinesByDefects(LocalDate startDate,
            LocalDate endDate) {
        List<Object[]> rawCounts = productionLogRepository.countDefectsByProductionLine(startDate,
                endDate);
        List<ProductionLineRankingDTO> rankings = new ArrayList<>(rawCounts.size());
        for (int index = 0; index < rawCounts.size(); index++) {
            Object[] rawCount = rawCounts.get(index);
            ProductionLineRankingDTO dto = new ProductionLineRankingDTO();
            dto.setLineName((String) rawCount[0]);
            dto.setTotalDefects(((Number) rawCount[1]).longValue());
            dto.setRank(index + 1);
            rankings.add(dto);
        }
        return rankings;
    }

    /**
     * Computes defect trend direction for each defect type. AC7: Compares current 7-day period
     * against the previous 7-day period and returns an indicator (INCREASING, DECREASING, STABLE).
     *
     * @param referenceDate
     *            the anchor date for the current period (typically today)
     * @return list of defect trends with direction indicators
     */
    public List<DefectTrendDTO> computeDefectTrends(LocalDate referenceDate) {
        LocalDate effectiveReferenceDate = referenceDate == null ? LocalDate.now() : referenceDate;

        LocalDate currentStart = effectiveReferenceDate.minusDays(6);
        LocalDate currentEnd = effectiveReferenceDate;
        LocalDate previousStart = currentStart.minusDays(7);
        LocalDate previousEnd = currentStart.minusDays(1);

        Map<String, Long> currentCounts = toCountMap(
                productionLogRepository.countDefectsByType(currentStart, currentEnd));
        Map<String, Long> previousCounts = toCountMap(
                productionLogRepository.countDefectsByType(previousStart, previousEnd));

        Set<String> defectNames = new TreeSet<>();
        defectNames.addAll(currentCounts.keySet());
        defectNames.addAll(previousCounts.keySet());

        List<DefectTrendDTO> trends = new ArrayList<>(defectNames.size());
        for (String defectName : defectNames) {
            long current = currentCounts.getOrDefault(defectName, 0L);
            long previous = previousCounts.getOrDefault(defectName, 0L);

            DefectTrendDTO dto = new DefectTrendDTO();
            dto.setDefectName(defectName);
            dto.setCurrentPeriodCount(current);
            dto.setPreviousPeriodCount(previous);
            dto.setTrendDirection(resolveTrendDirection(current, previous));
            trends.add(dto);
        }

        trends.sort(Comparator.comparing(DefectTrendDTO::getDefectName));
        return trends;
    }

    private Map<String, Long> toCountMap(List<Object[]> rawCounts) {
        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : rawCounts) {
            counts.put((String) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    private DefectTrendDTO.TrendDirection resolveTrendDirection(long current, long previous) {
        if (current > previous) {
            return DefectTrendDTO.TrendDirection.INCREASING;
        }
        if (current < previous) {
            return DefectTrendDTO.TrendDirection.DECREASING;
        }
        return DefectTrendDTO.TrendDirection.STABLE;
    }
}
