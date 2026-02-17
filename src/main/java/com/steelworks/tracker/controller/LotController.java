package com.steelworks.tracker.controller;

import com.steelworks.tracker.dto.LotDetailDTO;
import com.steelworks.tracker.service.LotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * LotController — Presentation layer controller for lot lookup and detail pages.
 *
 * <p>Supports the user story: "I want to look up files using lot IDs…"
 * by providing search-by-lot-ID functionality with fuzzy matching (AC2).</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /lots}           – list all lots.</li>
 *   <li>{@code GET /lots/search}    – search by lot ID (fuzzy, AC2).</li>
 *   <li>{@code GET /lots/{id}}      – view lot detail (AC1, AC3, AC4, AC9).</li>
 * </ul>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC1 – Data Integration: consolidated view of quality/shipping/production.</li>
 *   <li>AC2 – Fuzzy Matching: search tolerates formatting differences.</li>
 *   <li>AC3 – Shipping Status: shown in the detail view.</li>
 *   <li>AC4 – Line Attribution: shown in the detail view.</li>
 *   <li>AC9 – Source Transparency: source file/row shown in detail view.</li>
 * </ul>
 */
@Controller
@RequestMapping("/lots") // All endpoints in this controller start with /lots
public class LotController {

    private final LotService lotService;

    public LotController(LotService lotService) {
        this.lotService = lotService;
    }

    /**
     * List all lots in the system.
     *
     * @param model Spring MVC model
     * @return the "lots" template
     */
    @GetMapping
    public String listLots(Model model) {
        List<LotDetailDTO> lots = lotService.findAll();
        model.addAttribute("lots", lots);
        model.addAttribute("searchPerformed", false); // Haven't searched yet
        return "lots";
    }

    /**
     * Search for a lot by its ID using fuzzy matching (AC2).
     *
     * <p>The user enters a lot ID in the search box. This method normalises
     * the input and looks up the lot. If found, the detail is displayed inline.
     * If not found, a "no results" message is shown.</p>
     *
     * @param query the raw lot ID entered by the user
     * @param model Spring MVC model
     * @return the "lots" template with search results
     */
    @GetMapping("/search")
    public String searchLot(
            @RequestParam(name = "query", required = false, defaultValue = "") String query,
            Model model) {

        model.addAttribute("searchQuery", query);
        model.addAttribute("searchPerformed", true);

        if (query.isBlank()) {
            // Empty search: show all lots.
            model.addAttribute("lots", lotService.findAll());
        } else {
            // Fuzzy-match the query (AC2).
            Optional<LotDetailDTO> result = lotService.findByFuzzyId(query);
            if (result.isPresent()) {
                // Wrap single result in a list so the template can iterate uniformly.
                model.addAttribute("lots", List.of(result.get()));
            } else {
                // No match found.
                model.addAttribute("lots", List.of());
                model.addAttribute("noResults", true);
            }
        }

        return "lots";
    }

    /**
     * View the full detail of a specific lot by its exact identifier.
     *
     * <p>This endpoint is typically reached by clicking a lot row in the list
     * or dashboard. It shows all cross-referenced data (AC1), shipping status
     * (AC3), line attribution (AC4), and source metadata (AC9).</p>
     *
     * @param lotIdentifier the exact lot identifier (URL-encoded path variable)
     * @param model         Spring MVC model
     * @return the "lot-detail" template, or redirect to /lots if not found
     */
    @GetMapping("/{lotIdentifier}")
    public String lotDetail(
            @PathVariable String lotIdentifier,
            Model model) {

        Optional<LotDetailDTO> lotOpt = lotService.findByExactId(lotIdentifier);

        if (lotOpt.isPresent()) {
            model.addAttribute("lot", lotOpt.get());
            return "lot-detail";
        } else {
            // Lot not found: redirect to the list with an error message.
            return "redirect:/lots?error=notfound";
        }
    }
}
