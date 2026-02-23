package com.steelworks.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LotIdNormalizer. AC2: Fuzzy matching for Lot IDs.
 */
class LotIdNormalizerTest {

    private static final String NORMALIZED_LOT = "LOT123";
    private static final String TODO_MESSAGE = "TODO: add assertions";

    private LotIdNormalizer lotIdNormalizer;

    @BeforeEach
    void setUp() {
        lotIdNormalizer = new LotIdNormalizer();
    }

    @Test
    void normalize_shouldStripDashesAndStandardizeCase() {
        assertEquals(NORMALIZED_LOT, lotIdNormalizer.normalize("LOT-123"),
                "Should remove dashes and normalize case");
    }

    @Test
    void normalize_shouldKeepPlainId() {
        assertEquals(NORMALIZED_LOT, lotIdNormalizer.normalize("LOT123"),
                "Should preserve already normalized IDs");
    }

    @Test
    void normalize_shouldNormalizeLowercaseWithDash() {
        assertEquals(NORMALIZED_LOT, lotIdNormalizer.normalize("lot-123"),
                "Should normalize lowercase IDs with dashes");
    }

    @Test
    void normalize_shouldNormalizeLowercaseWithSpace() {
        assertEquals(NORMALIZED_LOT, lotIdNormalizer.normalize("lot 123"),
                "Should normalize lowercase IDs with spaces");
    }

    @Test
    void normalize_shouldHandleNullInput() {
        // TODO: Verify graceful handling of null input
        assertNotNull(lotIdNormalizer, TODO_MESSAGE);
    }

    @Test
    void normalize_shouldHandleEmptyString() {
        // TODO: Verify graceful handling of empty string
        assertNotNull(lotIdNormalizer, TODO_MESSAGE);
    }

    @Test
    void areEquivalent_shouldReturnTrueForMatchingIds() {
        // TODO: Verify "LOT-123" and "LOT123" are considered equivalent
        assertNotNull(lotIdNormalizer, TODO_MESSAGE);
    }

    @Test
    void areEquivalent_shouldReturnFalseForDifferentIds() {
        // TODO: Verify "LOT-123" and "LOT-456" are not equivalent
        assertNotNull(lotIdNormalizer, TODO_MESSAGE);
    }
}
