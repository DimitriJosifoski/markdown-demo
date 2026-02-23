package com.steelworks.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.steelworks.repository.ProductionLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for DefectAnalysisService. AC4: Line attribution. AC5: Production line ranking. AC7:
 * Defect trending.
 */
@ExtendWith(MockitoExtension.class)
class DefectAnalysisServiceTest {

    private static final String TODO_MESSAGE = "TODO: add assertions";

    @Mock
    private ProductionLogRepository productionLogRepository;

    @InjectMocks
    private DefectAnalysisService defectAnalysisService;

    @Test
    void getLineAttribution_shouldReturnCorrectProductionLine() {
        // TODO: AC4 - Verify the correct production line name is returned for a defect
        assertNotNull(defectAnalysisService, TODO_MESSAGE);
    }

    @Test
    void rankProductionLinesByDefects_shouldOrderByDefectCountDescending() {
        // TODO: AC5 - Verify production lines are ranked highest defect count first
        assertNotNull(defectAnalysisService, TODO_MESSAGE);
    }

    @Test
    void rankProductionLinesByDefects_shouldOnlyCountFlaggedDefects() {
        // TODO: AC5 - Verify only issue_flag=true entries are counted
        assertNotNull(defectAnalysisService, TODO_MESSAGE);
    }

    @Test
    void computeDefectTrends_shouldReturnIncreasingWhenCountRises() {
        // TODO: AC7 - Verify INCREASING trend when current > previous period
        assertNotNull(defectAnalysisService, TODO_MESSAGE);
    }

    @Test
    void computeDefectTrends_shouldReturnDecreasingWhenCountDrops() {
        // TODO: AC7 - Verify DECREASING trend when current < previous period
        assertNotNull(defectAnalysisService, TODO_MESSAGE);
    }

    @Test
    void computeDefectTrends_shouldReturnStableWhenCountUnchanged() {
        // TODO: AC7 - Verify STABLE trend when current == previous period
        assertNotNull(defectAnalysisService, TODO_MESSAGE);
    }
}
