package com.moderation.util;

import com.moderation.model.BannedWord;

import java.util.List;

/**
 * ============================================================
 * MODULE : Text Matcher
 * PERSON : Person 2 - Text Matching & Normalization
 * BRANCH : feature-text-matching
 * ============================================================
 *
 * Scans a piece of text against the banned-word dictionary and
 * returns the first match found.
 *
 * Detection capabilities:
 *  - Case-insensitive        ("KILL" detected as "kill")
 *  - Leet-speak / symbol     ("k1ll", "b@d", "b0mb" all detected)
 *  - Hyphenated / punctuated ("bad-word" detected as "badword")
 *  - Partial / substring     ("idiotkill" still triggers "kill")
 *
 * Both normalized and simple-normalized forms are checked so the
 * matcher is robust against all common obfuscation patterns.
 */
public class TextMatcher {

    /**
     * Scans {@code text} against every entry in {@code bannedWords}.
     *
     * @param text        The raw message to inspect
     * @param bannedWords The dictionary of forbidden words
     * @return The first {@link BannedWord} found, or {@code null} if the text is clean
     */
    public static BannedWord findMatch(String text, List<BannedWord> bannedWords) {
        if (text == null || bannedWords == null) return null;

        // Produce both forms of the input once (avoid re-normalizing in the loop)
        String normalized = TextNormalizer.normalize(text);       // leet-decoded
        String simple     = TextNormalizer.normalizeSimple(text); // just lowercased

        for (BannedWord bw : bannedWords) {
            String word = bw.getWord(); // already lowercase

            // 1. Leet-decoded form (catches k1ll -> kill, b@d -> bad, etc.)
            if (normalized.contains(word)) {
                return bw;
            }

            // 2. Simple form (catches KILL, drug, etc. after just lowercasing)
            if (simple.contains(word)) {
                return bw;
            }
        }
        return null; // no match -- content is clean
    }

    /**
     * Convenience helper: checks whether a specific word appears anywhere in text.
     * Uses full normalization so leet-speak and symbols are handled.
     *
     * @param text Raw user input
     * @param word The word to search for
     * @return true if the word is found (in any obfuscated form)
     */
    public static boolean containsWord(String text, String word) {
        if (text == null || word == null) return false;
        String normText = TextNormalizer.normalize(text);
        String normWord = TextNormalizer.normalize(word);
        return normText.contains(normWord);
    }
}
