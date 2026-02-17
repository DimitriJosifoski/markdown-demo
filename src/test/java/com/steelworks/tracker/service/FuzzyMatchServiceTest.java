package com.steelworks.tracker.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FuzzyMatchServiceTest — Unit tests for {@link FuzzyMatchService}.
 *
 * <h3>AC Coverage:</h3>
 * <ul>
 *   <li><b>AC2</b> — Fuzzy Matching for Lot IDs: every test in this class
 *       validates that the normalisation logic handles formatting variations.</li>
 * </ul>
 *
 * <p>These are pure unit tests — no Spring context, no database.
 * They run in milliseconds.</p>
 */
class FuzzyMatchServiceTest {

    // Create an instance directly (no Spring injection needed for a POJO service).
    private final FuzzyMatchService service = new FuzzyMatchService();

    // ════════════════════════════════════════════════════════════════════
    // AC2: normalize()
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC2: normalize strips hyphens → LOT-123 becomes LOT123")
    void normalize_stripsHyphens() {
        assertEquals("LOT123", service.normalize("LOT-123"));
    }

    @Test
    @DisplayName("AC2: normalize strips spaces → LOT 123 becomes LOT123")
    void normalize_stripsSpaces() {
        assertEquals("LOT123", service.normalize("LOT 123"));
    }

    @Test
    @DisplayName("AC2: normalize strips underscores → LOT_123 becomes LOT123")
    void normalize_stripsUnderscores() {
        assertEquals("LOT123", service.normalize("LOT_123"));
    }

    @Test
    @DisplayName("AC2: normalize converts to uppercase → lot-123 becomes LOT123")
    void normalize_uppercases() {
        assertEquals("LOT123", service.normalize("lot-123"));
    }

    @Test
    @DisplayName("AC2: normalize trims whitespace → '  LOT-123  ' becomes LOT123")
    void normalize_trims() {
        assertEquals("LOT123", service.normalize("  LOT-123  "));
    }

    @Test
    @DisplayName("AC2: normalize handles multiple consecutive separators → LOT--123 becomes LOT123")
    void normalize_multipleConsecutiveSeparators() {
        assertEquals("LOT123", service.normalize("LOT--123"));
    }

    @Test
    @DisplayName("AC2: normalize returns null for null input")
    void normalize_nullInput() {
        assertNull(service.normalize(null));
    }

    @Test
    @DisplayName("AC2: normalize handles empty string")
    void normalize_emptyString() {
        assertEquals("", service.normalize(""));
    }

    @Test
    @DisplayName("AC2: normalize handles complex real-world ID → LOT-20260112-001")
    void normalize_realWorldId() {
        assertEquals("LOT20260112001", service.normalize("LOT-20260112-001"));
    }

    // ════════════════════════════════════════════════════════════════════
    // AC2: isMatch()
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AC2: isMatch returns true for LOT-123 vs LOT123")
    void isMatch_hyphenVsNoHyphen() {
        assertTrue(service.isMatch("LOT-123", "LOT123"));
    }

    @Test
    @DisplayName("AC2: isMatch returns true for LOT 123 vs lot-123")
    void isMatch_spaceVsHyphenAndCase() {
        assertTrue(service.isMatch("LOT 123", "lot-123"));
    }

    @Test
    @DisplayName("AC2: isMatch returns false for different IDs")
    void isMatch_differentIds() {
        assertFalse(service.isMatch("LOT-123", "LOT-456"));
    }

    @Test
    @DisplayName("AC2: isMatch returns true when both are null")
    void isMatch_bothNull() {
        assertTrue(service.isMatch(null, null));
    }

    @Test
    @DisplayName("AC2: isMatch returns false when one is null")
    void isMatch_oneNull() {
        assertFalse(service.isMatch("LOT-123", null));
        assertFalse(service.isMatch(null, "LOT-123"));
    }
}
