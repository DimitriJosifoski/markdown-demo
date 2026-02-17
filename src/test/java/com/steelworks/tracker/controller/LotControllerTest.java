package com.steelworks.tracker.controller;

import com.steelworks.tracker.dto.LotDetailDTO;
import com.steelworks.tracker.service.LotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LotControllerTest — Integration tests for the {@link LotController}.
 *
 * <h3>AC Coverage:</h3>
 * <ul>
 *   <li><b>AC1</b> — Cross-referencing: lot detail page shows joined data.</li>
 *   <li><b>AC2</b> — Fuzzy Matching: search endpoint delegates to fuzzy service.</li>
 *   <li><b>AC3</b> — Shipping Status: lot detail shows status.</li>
 *   <li><b>AC4</b> — Line Attribution: lot detail shows production line.</li>
 *   <li><b>AC9</b> — Source Transparency: lot detail shows source metadata.</li>
 * </ul>
 */
@WebMvcTest(LotController.class)
class LotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotService lotService;

    /** Create a sample lot detail for testing. */
    private LotDetailDTO sampleLot() {
        return new LotDetailDTO(
                "LOT-20260112-001",
                "SKU-100",
                "Shipped",          // AC3
                "Line 1",           // AC4
                "Surface Crack",    // Defect from production data
                "production.csv",   // AC9
                42                  // AC9
        );
    }

    // ════════════════════════════════════════════════════════════════════
    // AC1: List all lots
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC1: GET /lots returns 200 and renders lots template")
    void listLots_returnsOk() throws Exception {
        when(lotService.findAll()).thenReturn(List.of(sampleLot()));

        mockMvc.perform(get("/lots"))
                .andExpect(status().isOk())
                .andExpect(view().name("lots"))
                .andExpect(model().attributeExists("lots"));
    }

    // ════════════════════════════════════════════════════════════════════
    // AC2: Fuzzy search
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC2: GET /lots/search?query=LOT 20260112 001 finds lot via fuzzy match")
    void searchLot_fuzzyMatchFound() throws Exception {
        when(lotService.findByFuzzyId("LOT 20260112 001"))
                .thenReturn(Optional.of(sampleLot()));

        mockMvc.perform(get("/lots/search").param("query", "LOT 20260112 001"))
                .andExpect(status().isOk())
                .andExpect(view().name("lots"))
                .andExpect(model().attribute("lots",
                        org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    @DisplayName("AC2: GET /lots/search?query=NONEXISTENT shows noResults")
    void searchLot_fuzzyMatchNotFound() throws Exception {
        when(lotService.findByFuzzyId("NONEXISTENT"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/lots/search").param("query", "NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("lots"))
                .andExpect(model().attribute("noResults", true));
    }

    // ════════════════════════════════════════════════════════════════════
    // AC1 + AC3 + AC4 + AC9: Lot detail page
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC1, AC3, AC4, AC9: GET /lots/{id} returns lot detail")
    void lotDetail_returnsDetail() throws Exception {
        when(lotService.findByExactId("LOT-20260112-001"))
                .thenReturn(Optional.of(sampleLot()));

        mockMvc.perform(get("/lots/LOT-20260112-001"))
                .andExpect(status().isOk())
                .andExpect(view().name("lot-detail"))
                .andExpect(model().attributeExists("lot"));
    }

    @Test
    @DisplayName("AC1: GET /lots/{id} redirects when lot not found")
    void lotDetail_redirectsWhenNotFound() throws Exception {
        when(lotService.findByExactId(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/lots/NONEXISTENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lots?error=notfound"));
    }
}
