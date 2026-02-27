package com.moderation.util;

import com.moderation.model.BannedWord;

import java.util.List;

/**
 * Matches normalized text against a list of banned words.
 * Supports:
 *  - Case-insensitive matching
 *  - Partial / substring matching
 *  - Symbol-stripped / leet-speak matching (via TextNormalizer)
 */
public class TextMatcher {

    /**
     * Returns the first BannedWord found in the text, or null if clean.
     */
    public static BannedWord findMatch(String text, List<BannedWord> bannedWords) {
        if (text == null || bannedWords == null) return null;

        String normalized = TextNormalizer.normalize(text);
        String simple     = TextNormalizer.normalizeSimple(text);

        for (BannedWord bw : bannedWords) {
            String bwWord = bw.getWord(); // already lowercase

            // 1. Direct substring match in fully normalized text
            if (normalized.contains(bwWord)) {
                return bw;
            }

            // 2. Substring match in simply-normalized text (catches case differences + basic symbols)
            if (simple.contains(bwWord)) {
                return bw;
            }
        }
        return null;
    }

    /**
     * Returns true if the text contains the exact word (case-insensitive, symbol-stripped).
     */
    public static boolean containsWord(String text, String word) {
        if (text == null || word == null) return false;
        String normText = TextNormalizer.normalize(text);
        String normWord = TextNormalizer.normalize(word);
        return normText.contains(normWord);
    }
}
