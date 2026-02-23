package com.steelworks.controller;

import com.steelworks.dto.DashboardSummaryDTO;
import com.steelworks.enums.TimeGrouping;
import com.steelworks.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Summary Dashboard ("Meeting Ready" view). AC5: Production line rankings.
 * AC6: Shipping risk alerts. AC7: Defect trending. AC8: Supports time grouping toggle (DAILY,
 * WEEKLY, MONTHLY); defaults to WEEKLY.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Returns the full dashboard summary for the selected time grouping. AC8: Defaults to WEEKLY if
     * no time grouping is provided. Includes production line ranking (AC5), shipping risk alerts
     * (AC6), and defect trends (AC7).
     *
     * @param timeGrouping
     *            optional time grouping (DAILY, WEEKLY, MONTHLY)
     * @return dashboard summary DTO
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(
            @RequestParam(required = false, defaultValue = "WEEKLY") TimeGrouping timeGrouping) {
        // TODO: Delegate to DashboardService.getDashboardSummary()
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
