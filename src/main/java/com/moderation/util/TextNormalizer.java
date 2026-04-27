package com.moderation.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * MODULE : Text Normalizer
 * PERSON : Person 2 - Text Matching & Normalization
 * BRANCH : feature-text-matching
 * ============================================================
 *
 * Converts raw user input into a clean, comparable string so that
 * evaded / disguised words can still be detected.
 *
 * Normalization pipeline:
 *  1. Lowercase
 *  2. Leet-speak and symbol substitution  (@ -> a, 0 -> o, 1 -> i, etc.)
 *  3. Strip remaining non-alphanumeric characters (hyphens, dots, etc.)
 *
 * Examples:
 *  "KILL"       -> "kill"
 *  "k1ll"       -> "kill"
 *  "b@dword"    -> "badword"
 *  "b0mb"       -> "bomb"
 *  "bad-word"   -> "badword"
 *  "$cam"       -> "scam"
 */
public class TextNormalizer {

    /**
     * Common leet-speak / symbol -> letter substitutions.
     * Evaluated AFTER lowercasing, so only need lower-case keys.
     */
    private static final Map<Character, Character> SUBSTITUTIONS = new HashMap<>();

    static {
        SUBSTITUTIONS.put('@', 'a');
        SUBSTITUTIONS.put('4', 'a');
        SUBSTITUTIONS.put('3', 'e');
        SUBSTITUTIONS.put('1', 'i');
        SUBSTITUTIONS.put('!', 'i');
        SUBSTITUTIONS.put('0', 'o');
        SUBSTITUTIONS.put('5', 's');
        SUBSTITUTIONS.put('$', 's');
        SUBSTITUTIONS.put('7', 't');
        SUBSTITUTIONS.put('+', 't');
        SUBSTITUTIONS.put('9', 'g');
        SUBSTITUTIONS.put('6', 'b');
        SUBSTITUTIONS.put('8', 'b');
    }

    /**
     * Full normalization:
     *   lowercase -> leet-speak substitution -> strip remaining non-alphanumeric chars.
     *
     * This is the primary method used by TextMatcher to defeat obfuscation.
     *
     * @param text Raw user input
     * @return Fully normalized text, suitable for banned-word lookup
     */
    public static String normalize(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (char c : lower.toCharArray()) {
            if (SUBSTITUTIONS.containsKey(c)) {
                sb.append(SUBSTITUTIONS.get(c));
            } else if (Character.isLetterOrDigit(c) || c == ' ') {
                sb.append(c);
            }
            // Drop hyphens, dots, underscores, etc.
        }
        return sb.toString().trim();
    }

    /**
     * Lightweight normalization:
     *   lowercase only + strip non-alphanumeric characters (spaces preserved).
     *
     * Used for simple case-insensitive checks without leet substitution.
     *
     * @param text Raw user input
     * @return Lowercased text with symbols removed
     */
    public static String normalizeSimple(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }

    /**
     * Utility: returns the leet-substitution map (for display / documentation).
     */
    public static Map<Character, Character> getSubstitutionMap() {
        return java.util.Collections.unmodifiableMap(SUBSTITUTIONS);
    }
}
