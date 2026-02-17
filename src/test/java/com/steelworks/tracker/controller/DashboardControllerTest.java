package com.steelworks.tracker.controller;

import com.steelworks.tracker.dto.*;
import com.steelworks.tracker.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DashboardControllerTest — Integration tests for the {@link DashboardController}.
 *
 * <p>Uses {@code @WebMvcTest} which loads ONLY the web layer (no database,
 * no full application context). The {@link DashboardService} is replaced
 * with a Mockito mock via {@code @MockBean}.</p>
 *
 * <p>These tests verify:</p>
 * <ul>
 *   <li>HTTP status codes (200 OK).</li>
 *   <li>Correct Thymeleaf template is rendered.</li>
 *   <li>Model attributes contain the expected data.</li>
 * </ul>
 *
 * <h3>AC Coverage:</h3>
 * <ul>
 *   <li><b>AC5</b>  — Dashboard renders production line ranking section.</li>
 *   <li><b>AC6</b>  — Dashboard renders shipping risk alert section.</li>
 *   <li><b>AC7</b>  — Dashboard renders defect trending section.</li>
 *   <li><b>AC8</b>  — Dashboard supports time-grouping query parameter.</li>
 *   <li><b>AC10</b> — Dashboard renders orphaned data section.</li>
 *   <li><b>AC11</b> — Dashboard renders data conflict section.</li>
 * </ul>
 */
@WebMvcTest(DashboardController.class) // Only loads DashboardController + web layer.
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc; // Spring test utility for simulating HTTP requests.

    @MockBean
    private DashboardService dashboardService; // Replaces real service with a mock.

    /**
     * Create a sample DashboardDTO for testing.
     */
    private DashboardDTO createSampleDashboard() {
        return new DashboardDTO(
                // AC5: Line rankings
                List.of(new LineDefectCountDTO("Line 1", 5, 1)),
                // AC6: Shipping risks
                List.of(new ShippingRiskDTO("LOT-001", "Acme Corp",
                        LocalDate.of(2026, 2, 10), "Surface Crack", "Critical")),
                // AC7: Defect trends
                List.of(new DefectTrendDTO("Surface Crack", "Critical", 5, 2, "UP")),
                // AC10: Orphaned lots
                List.of(new OrphanedLotDTO("LOT-ORPHAN", "SKU-X", "Orphaned Data")),
                // AC11: Data conflicts
                List.of(new DataConflictDTO("LOT-CONFLICT", 2)),
                // Period
                LocalDate.of(2026, 2, 9),
                LocalDate.of(2026, 2, 15),
                // AC8: Time grouping
                "WEEKLY"
        );
    }

    @Test
    @DisplayName("AC5-8, AC10-11: GET / returns 200 and renders dashboard template")
    void rootUrl_returnsDashboard() throws Exception {
        when(dashboardService.buildDashboard(any())).thenReturn(createSampleDashboard());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())                    // HTTP 200
                .andExpect(view().name("dashboard"))           // Correct template
                .andExpect(model().attributeExists("dashboard")); // Model has the DTO
    }

    @Test
    @DisplayName("AC8: GET /dashboard?timeGrouping=DAILY passes param to service")
    void dashboardWithDailyToggle() throws Exception {
        DashboardDTO dailyDashboard = new DashboardDTO(
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(),
                LocalDate.now(), LocalDate.now(), "DAILY"
        );
        when(dashboardService.buildDashboard("DAILY")).thenReturn(dailyDashboard);

        mockMvc.perform(get("/dashboard").param("timeGrouping", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @DisplayName("AC5: Dashboard template contains line ranking data in the model")
    void dashboardContainsLineRankings() throws Exception {
        when(dashboardService.buildDashboard(any())).thenReturn(createSampleDashboard());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("dashboard"))
                .andExpect(result -> {
                    DashboardDTO dto = (DashboardDTO) result.getModelAndView().getModel().get("dashboard");
                    assertEquals(1, dto.lineRankings().size());
                });
    }

    @Test
    @DisplayName("AC6: Dashboard template contains shipping risk data in the model")
    void dashboardContainsShippingRisks() throws Exception {
        when(dashboardService.buildDashboard(any())).thenReturn(createSampleDashboard());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("dashboard"))
                .andExpect(result -> {
                    DashboardDTO dto = (DashboardDTO) result.getModelAndView().getModel().get("dashboard");
                    assertEquals(1, dto.shippingRisks().size());
                });
    }
}
