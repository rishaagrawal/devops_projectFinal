package com.moderation;

import com.moderation.model.BannedWord;
import com.moderation.model.Severity;
import com.moderation.util.TextMatcher;
import com.moderation.util.TextNormalizer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TextMatchingTest {

    // ---- TextNormalizer Tests ----

    @Test
    void testNormalizeLowercase() {
        String result = TextNormalizer.normalize("HELLO WORLD");
        assertEquals("hello world", result,
                "Normalize should convert to lowercase");
    }

    @Test
    void testNormalizeAtSymbolToA() {
        String result = TextNormalizer.normalize("b@d");
        assertEquals("bad", result,
                "@ should be substituted with 'a'");
    }

    @Test
    void testNormalizeZeroToO() {
        String result = TextNormalizer.normalize("b0mb");
        assertEquals("bomb", result,
                "0 should be substituted with 'o'");
    }

    @Test
    void testNormalizeOneToI() {
        String result = TextNormalizer.normalize("k1ll");
        assertEquals("kill", result,
                "1 should be substituted with 'i'");
    }

    @Test
    void testNormalizeDollarToS() {
        String result = TextNormalizer.normalize("$cam");
        assertEquals("scam", result,
                "$ should be substituted with 's'");
    }

    @Test
    void testNormalizeStripsHyphens() {
        String result = TextNormalizer.normalize("bad-word");
        assertEquals("badword", result,
                "Hyphens should be stripped");
    }

    @Test
    void testNormalizeStripsUnderscores() {
        String result = TextNormalizer.normalize("bad_word");
        assertEquals("badword", result,
                "Underscores should be stripped");
    }

    @Test
    void testNormalizeCombined() {
        String result = TextNormalizer.normalize("K1LL");
        assertEquals("kill", result,
                "Combined leet + uppercase should normalize correctly");
    }

    @Test
    void testNormalizeNullReturnsEmpty() {
        String result = TextNormalizer.normalize(null);
        assertEquals("", result,
                "Null input should return empty string");
    }

    // ---- TextMatcher Tests ----

    private List<BannedWord> sampleWords() {
        return Arrays.asList(
                new BannedWord("kill",  Severity.HIGH),
                new BannedWord("bomb",  Severity.HIGH),
                new BannedWord("idiot", Severity.LOW)
        );
    }

    @Test
    void testCaseInsensitiveDetection() {
        BannedWord result = TextMatcher.findMatch("KILL everyone", sampleWords());
        assertNotNull(result, "Should detect 'KILL' case-insensitively");
        assertEquals("kill", result.getWord());
    }

    @Test
    void testSymbolStrippedDetection() {
        BannedWord result = TextMatcher.findMatch("b0mb attack", sampleWords());
        assertNotNull(result, "Should detect 'b0mb' as 'bomb' via leet substitution");
        assertEquals("bomb", result.getWord());
    }

    @Test
    void testPartialMatchDetection() {
        BannedWord result = TextMatcher.findMatch("he is a total idiotface", sampleWords());
        assertNotNull(result, "Should detect 'idiot' as a partial match");
        assertEquals("idiot", result.getWord());
    }

    @Test
    void testAtSymbolEvasion() {
        BannedWord result = TextMatcher.findMatch("k1ll the target", sampleWords());
        assertNotNull(result, "Should detect 'k1ll' disguised word");
        assertEquals("kill", result.getWord());
    }

    @Test
    void testCleanTextReturnsNull() {
        BannedWord result = TextMatcher.findMatch("Hello, have a nice day!", sampleWords());
        assertNull(result, "Clean text should return null");
    }

    @Test
    void testContainsWordHelper() {
        assertTrue(TextMatcher.containsWord("KILL the player", "kill"),
                "containsWord should be case-insensitive");
        assertFalse(TextMatcher.containsWord("Hello world", "kill"),
                "Should not find word not present");
    }
}
