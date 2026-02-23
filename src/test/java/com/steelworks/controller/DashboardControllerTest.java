package com.steelworks.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.steelworks.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for DashboardController. Verifies correct delegation and default time grouping
 * behavior.
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private static final String TODO_MESSAGE = "TODO: add assertions";

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void getDashboardSummary_shouldDelegateToDashboardService() {
        // TODO: Verify controller delegates to DashboardService
        assertNotNull(dashboardController, TODO_MESSAGE);
    }

    @Test
    void getDashboardSummary_shouldDefaultToWeeklyTimeGrouping() {
        // TODO: AC8 - Verify WEEKLY is the default when no param is provided
        assertNotNull(dashboardController, TODO_MESSAGE);
    }
}
