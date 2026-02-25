package com.steelworks.util;

import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Utility for normalizing and fuzzy-matching Lot IDs. AC2: Recognizes and links Lot IDs with minor
 * formatting differences (e.g., "LOT123" vs "LOT-123").
 */
@Component
public class LotIdNormalizer {

    /**
     * Normalizes a raw Lot ID by stripping formatting differences (dashes, spaces, case) to produce
     * a canonical form.
     *
     * @param rawLotId
     *            the Lot ID as entered or imported
     * @return the normalized form for comparison
     */
    public String normalize(String rawLotId) {
        if (rawLotId == null) {
            return null;
        }
        return rawLotId.replaceAll("[\\-\\s]", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Determines if two Lot IDs refer to the same lot after normalization.
     *
     * @param lotId1
     *            first Lot ID
     * @param lotId2
     *            second Lot ID
     * @return true if they match after normalization
     */
    public boolean areEquivalent(String lotId1, String lotId2) {
        String normalizedLotId1 = normalize(lotId1);
        String normalizedLotId2 = normalize(lotId2);
        if (normalizedLotId1 == null || normalizedLotId2 == null) {
            return false;
        }
        return normalizedLotId1.equals(normalizedLotId2);
    }
}
