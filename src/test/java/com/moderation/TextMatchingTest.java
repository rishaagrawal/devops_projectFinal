package com.moderation;

import com.moderation.model.BannedWord;
import com.moderation.model.Severity;
import com.moderation.util.TextMatcher;
import com.moderation.util.TextNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TEST MODULE : Text Matching & Normalization Tests
 * PERSON      : Person 2 - Text Matching & Normalization
 * BRANCH      : feature-text-matching
 * ============================================================
 *
 * Tests that TextNormalizer correctly decodes leet-speak / symbols
 * and that TextMatcher finds banned words in all obfuscated forms.
 *
 * Run with Maven: mvn test -pl . -Dtest=TextMatchingTest
 */
@DisplayName("Person 2 - Text Matching & Normalization Tests")
public class TextMatchingTest {

    private List<BannedWord> dictionary;

    @BeforeEach
    void setUp() {
        dictionary = Arrays.asList(
                new BannedWord("kill",   Severity.HIGH),
                new BannedWord("bomb",   Severity.HIGH),
                new BannedWord("drug",   Severity.MEDIUM),
                new BannedWord("idiot",  Severity.LOW),
                new BannedWord("scam",   Severity.MEDIUM)
        );
    }

    // ?? TextNormalizer: lowercase ?????????????????????????????????????

    @Test
    @DisplayName("normalize() lowercases all characters")
    void testNormalizeLowercase() {
        assertEquals("kill", TextNormalizer.normalize("KILL"),
                "normalize should convert to lowercase");
    }

    @Test
    @DisplayName("normalize() handles mixed case")
    void testNormalizeMixedCase() {
        assertEquals("bomb", TextNormalizer.normalize("BoMb"),
                "normalize should handle mixed case");
    }

    // ?? TextNormalizer: leet-speak ????????????????????????????????????

    @Test
    @DisplayName("normalize() decodes 1 -> i (k1ll -> kill)")
    void testNormalizeLeetI() {
        assertEquals("kill", TextNormalizer.normalize("k1ll"),
                "leet '1' should be decoded to 'i'");
    }

    @Test
    @DisplayName("normalize() decodes 0 -> o (b0mb -> bomb)")
    void testNormalizeLeetO() {
        assertEquals("bomb", TextNormalizer.normalize("b0mb"),
                "leet '0' should be decoded to 'o'");
    }

    @Test
    @DisplayName("normalize() decodes @ -> a (b@d -> bad)")
    void testNormalizeLeetAt() {
        assertEquals("bad", TextNormalizer.normalize("b@d"),
                "leet '@' should be decoded to 'a'");
    }

    @Test
    @DisplayName("normalize() decodes $ -> s ($cam -> scam)")
    void testNormalizeSymbolS() {
        assertEquals("scam", TextNormalizer.normalize("$cam"),
                "'$' should be decoded to 's'");
    }

    @Test
    @DisplayName("normalize() strips hyphens (bad-word -> badword)")
    void testNormalizeStripsHyphen() {
        String result = TextNormalizer.normalize("bad-word");
        assertFalse(result.contains("-"),
                "Hyphens should be stripped during normalization");
    }

    // ?? TextNormalizer: normalizeSimple ???????????????????????????????

    @Test
    @DisplayName("normalizeSimple() lowercases and strips symbols but NOT leet")
    void testNormalizeSimple() {
        String result = TextNormalizer.normalizeSimple("B0MB!");
        assertEquals("b0mb", result,
                "normalizeSimple should lowercase and strip non-alphanumeric, but keep digits");
    }

    @Test
    @DisplayName("normalize(null) returns empty string")
    void testNormalizeNull() {
        assertEquals("", TextNormalizer.normalize(null),
                "null input should produce empty string");
    }

    // ?? TextMatcher: case-insensitive detection ???????????????????????

    @Test
    @DisplayName("TextMatcher detects uppercase version of banned word")
    void testDetectUppercase() {
        BannedWord match = TextMatcher.findMatch("KILL them all", dictionary);
        assertNotNull(match, "Uppercase banned word should be detected");
        assertEquals("kill", match.getWord());
    }

    @Test
    @DisplayName("TextMatcher finds clean text as null")
    void testCleanTextReturnsNull() {
        BannedWord match = TextMatcher.findMatch("Hello, have a nice day!", dictionary);
        assertNull(match, "Clean text should return null");
    }

    // ?? TextMatcher: leet-speak detection ????????????????????????????

    @Test
    @DisplayName("TextMatcher detects k1ll (leet for kill)")
    void testDetectLeetKill() {
        BannedWord match = TextMatcher.findMatch("I will k1ll you", dictionary);
        assertNotNull(match, "Leet-speak 'k1ll' should be detected");
        assertEquals("kill", match.getWord());
    }

    @Test
    @DisplayName("TextMatcher detects b0mb (leet for bomb)")
    void testDetectLeetBomb() {
        BannedWord match = TextMatcher.findMatch("plant a b0mb", dictionary);
        assertNotNull(match, "Leet-speak 'b0mb' should be detected");
        assertEquals("bomb", match.getWord());
    }

    @Test
    @DisplayName("TextMatcher detects $cam (symbol evasion for scam)")
    void testDetectSymbolScam() {
        BannedWord match = TextMatcher.findMatch("This is a $cam!", dictionary);
        assertNotNull(match, "Symbol evasion '$cam' should be detected");
        assertEquals("scam", match.getWord());
    }

    // ?? TextMatcher: partial / substring match ????????????????????????

    @Test
    @DisplayName("TextMatcher detects banned word embedded in larger string")
    void testDetectPartialMatch() {
        BannedWord match = TextMatcher.findMatch("killzone is a game", dictionary);
        assertNotNull(match, "Partial match 'killzone' should trigger 'kill'");
    }

    // ?? TextMatcher.containsWord() ????????????????????????????????????

    @Test
    @DisplayName("containsWord returns true for present leet word")
    void testContainsWordTrue() {
        assertTrue(TextMatcher.containsWord("I will k1ll you", "kill"),
                "containsWord should return true for leet-encoded word");
    }

    @Test
    @DisplayName("containsWord returns false for absent word")
    void testContainsWordFalse() {
        assertFalse(TextMatcher.containsWord("Hello world", "bomb"),
                "containsWord should return false when word is absent");
    }

    @Test
    @DisplayName("containsWord handles null text gracefully")
    void testContainsWordNullText() {
        assertFalse(TextMatcher.containsWord(null, "kill"),
                "null text should return false");
    }
}
