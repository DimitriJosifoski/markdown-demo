package com.steelworks.tracker.service;

import org.springframework.stereotype.Service;

/**
 * FuzzyMatchService — Implements the Lot ID fuzzy-matching logic (AC2).
 *
 * <h3>Problem:</h3>
 * <p>Different source files may record the same lot with minor formatting
 * variations: "LOT123", "LOT-123", "lot 123", "LOT_123". We need to treat
 * all of these as the <em>same</em> lot.</p>
 *
 * <h3>Strategy:</h3>
 * <p>Rather than doing expensive fuzzy searches at query time (LIKE, regex,
 * Levenshtein distance), we <b>normalise on write</b>:</p>
 * <ol>
 *   <li>When a lot is inserted, compute a "normalised" version of the ID.</li>
 *   <li>Store it in the {@code normalized_lot_id} column.</li>
 *   <li>When looking up a lot, normalise the search term the same way and
 *       query the normalised column for an exact match.</li>
 * </ol>
 *
 * <p>This gives us <b>O(log n)</b> lookup via the database index instead of
 * <b>O(n × m)</b> fuzzy comparison across all rows.</p>
 *
 * <h3>Normalisation rules (applied in order):</h3>
 * <ol>
 *   <li>Convert to UPPERCASE.</li>
 *   <li>Remove hyphens ({@code -}), underscores ({@code _}), and spaces.</li>
 *   <li>Trim leading/trailing whitespace.</li>
 * </ol>
 *
 * <h3>Related ACs:</h3>
 * <ul>
 *   <li>AC2 – Fuzzy Matching for Lot IDs.</li>
 * </ul>
 */
@Service  // Registers this class as a Spring-managed service bean.
public class FuzzyMatchService {

    /**
     * Normalise a raw lot identifier into a canonical form for matching.
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>"LOT-123"   → "LOT123"</li>
     *   <li>"lot 123"   → "LOT123"</li>
     *   <li>"LOT_123"   → "LOT123"</li>
     *   <li>"  LOT--123 " → "LOT123"</li>
     * </ul>
     *
     * @param rawLotId the original lot identifier string (may be null)
     * @return the normalised string, or {@code null} if input is null
     *
     * <p><b>Time complexity:</b>  O(n) where n = length of the string.</p>
     * <p><b>Space complexity:</b> O(n) for the new string.</p>
     */
    public String normalize(String rawLotId) {
        // Guard clause: return null for null input to avoid NullPointerException.
        if (rawLotId == null) {
            return null;
        }

        return rawLotId
                .trim()                         // Step 1: strip leading/trailing whitespace
                .toUpperCase()                   // Step 2: normalise case to uppercase
                .replaceAll("[\\-_ ]+", "");     // Step 3: remove hyphens, underscores, spaces
                // The regex "[\\-_ ]+" matches one or more of: hyphen, underscore, space.
                // "+" ensures consecutive separators are collapsed in one pass.
    }

    /**
     * Check whether two raw lot IDs refer to the same lot after normalisation.
     *
     * @param rawId1 first lot ID (e.g., "LOT-123")
     * @param rawId2 second lot ID (e.g., "LOT 123")
     * @return {@code true} if they normalise to the same string
     *
     * <p><b>Time complexity:</b>  O(n + m) where n, m are the string lengths.</p>
     * <p><b>Space complexity:</b> O(n + m) for the two normalised strings.</p>
     */
    public boolean isMatch(String rawId1, String rawId2) {
        // Normalise both and compare.  Uses Objects-style null safety.
        String norm1 = normalize(rawId1);
        String norm2 = normalize(rawId2);

        // If both are null, consider them "matching" (both absent).
        if (norm1 == null && norm2 == null) {
            return true;
        }
        // If only one is null, they don't match.
        if (norm1 == null || norm2 == null) {
            return false;
        }
        return norm1.equals(norm2);
    }
}
