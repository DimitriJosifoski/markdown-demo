package com.steelworks.tracker.controller;

import com.steelworks.tracker.dto.DashboardDTO;
import com.steelworks.tracker.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * DashboardController — Presentation layer controller for the "Meeting Ready"
 * summary dashboard.
 *
 * <p>Handles HTTP GET requests to the root URL ({@code /}) and the
 * {@code /dashboard} endpoint. Delegates all business logic to
 * {@link DashboardService} and passes the resulting DTO to the Thymeleaf
 * template for rendering.</p>
 *
 * <h3>How Spring MVC works here:</h3>
 * <ol>
 *   <li>User navigates to {@code http://localhost:8080/} in their browser.</li>
 *   <li>Spring matches the URL to {@link #dashboard(String, Model)}.</li>
 *   <li>The method calls the service layer, populates the {@link Model}, and
 *       returns the template name {@code "dashboard"}.</li>
 *   <li>Thymeleaf resolves this to {@code templates/dashboard.html} and
 *       renders the HTML using the model attributes.</li>
 * </ol>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC5  – Production Line Ranking (table on dashboard).</li>
 *   <li>AC6  – Shipping Risk Alert (alert section).</li>
 *   <li>AC7  – Defect Trending (trend arrows).</li>
 *   <li>AC8  – Default Time-Grouping with toggle.</li>
 *   <li>AC10 – Orphaned Data section.</li>
 *   <li>AC11 – Data Conflict section.</li>
 * </ul>
 */
@Controller  // Marks this as a Spring MVC controller (returns view names, not JSON).
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Constructor injection of the dashboard service.
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Serve the main dashboard page.
     *
     * <p>{@code @GetMapping} maps HTTP GET requests for "/" and "/dashboard" to
     * this method.  The {@code timeGrouping} query parameter supports the
     * AC8 toggle: {@code /dashboard?timeGrouping=DAILY}.</p>
     *
     * <p>If no query parameter is provided, AC8 specifies the default is "WEEKLY".</p>
     *
     * @param timeGrouping the time grouping selection ("DAILY", "WEEKLY", "MONTHLY");
     *                     defaults to "WEEKLY" if absent (AC8)
     * @param model        the Spring MVC model — acts as a key-value bag that
     *                     Thymeleaf templates can access via {@code ${key}}
     * @return the name of the Thymeleaf template to render ("dashboard")
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(
            @RequestParam(name = "timeGrouping", required = false) String timeGrouping,
            Model model) {

        // Build the complete dashboard DTO with all sections.
        DashboardDTO dashboard = dashboardService.buildDashboard(timeGrouping);

        // Add the DTO to the model so Thymeleaf can access it as ${dashboard}.
        model.addAttribute("dashboard", dashboard);

        // Return the template name (resolves to: templates/dashboard.html).
        return "dashboard";
    }
}
