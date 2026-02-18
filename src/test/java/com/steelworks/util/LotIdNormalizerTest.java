package com.steelworks.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LotIdNormalizer.
 * AC2: Fuzzy matching for Lot IDs.
 */
class LotIdNormalizerTest {

    private LotIdNormalizer lotIdNormalizer;

    @BeforeEach
    void setUp() {
        lotIdNormalizer = new LotIdNormalizer();
    }

    @Test
    void normalize_shouldStripDashesAndStandardizeCase() {
        assertEquals("LOT123", lotIdNormalizer.normalize("LOT-123"));
        assertEquals("LOT123", lotIdNormalizer.normalize("LOT123"));
        assertEquals("LOT123", lotIdNormalizer.normalize("lot-123"));
        assertEquals("LOT123", lotIdNormalizer.normalize("lot 123"));
    }

    @Test
    void normalize_shouldHandleNullInput() {
        // TODO: Verify graceful handling of null input
    }

    @Test
    void normalize_shouldHandleEmptyString() {
        // TODO: Verify graceful handling of empty string
    }

    @Test
    void areEquivalent_shouldReturnTrueForMatchingIds() {
        // TODO: Verify "LOT-123" and "LOT123" are considered equivalent
    }

    @Test
    void areEquivalent_shouldReturnFalseForDifferentIds() {
        // TODO: Verify "LOT-123" and "LOT-456" are not equivalent
    }
}
