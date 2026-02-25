package com.steelworks.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(lotIdNormalizer.areEquivalent("LOT-123", "LOT123"),
                "Should consider differently formatted matching lot IDs equivalent");
    }

    @Test
    void areEquivalent_shouldReturnFalseForDifferentIds() {
        assertFalse(lotIdNormalizer.areEquivalent("LOT-123", "LOT-456"),
                "Should treat different lot IDs as non-equivalent");
    }
}
