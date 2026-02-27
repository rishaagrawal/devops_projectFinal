package com.moderation.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles text normalization:
 *  - Lowercase conversion
 *  - Symbol / leet-speak substitution (b@d → bad)
 *  - Stripping remaining non-alphanumeric characters
 */
public class TextNormalizer {

    // Common leet-speak / symbol substitutions
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
     * Full normalization: lowercase → leet substitution → strip remaining symbols.
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
            // drop everything else (hyphens, dots, underscores, etc.)
        }
        return sb.toString().trim();
    }

    /**
     * Lightweight normalization: just lowercase + strip non-alphanumeric (keep spaces).
     * Used for quick case-insensitive checks without leet substitution.
     */
    public static String normalizeSimple(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }
}
