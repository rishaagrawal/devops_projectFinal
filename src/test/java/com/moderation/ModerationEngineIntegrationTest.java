package com.moderation;

import com.moderation.engine.ModerationEngine;
import com.moderation.model.Context;
import com.moderation.model.ModerationAction;
import com.moderation.model.ModerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TEST MODULE : Moderation Engine Integration Tests
 * PERSON      : Person 4 - Audit Logs & Testing
 * BRANCH      : feature-audit-logs
 * ============================================================
 *
 * End-to-end pipeline tests:
 *  Text Input -> Normalization -> Matching -> Severity -> Context -> Strikes -> Audit Log
 *
 * These tests verify that all four modules interact correctly
 * through the ModerationEngine orchestrator.
 *
 * Run with Maven: mvn test -pl . -Dtest=ModerationEngineIntegrationTest
 * Run ALL tests : mvn test
 */
@DisplayName("Person 4 - Integration Tests (Full Pipeline)")
public class ModerationEngineIntegrationTest {

    private ModerationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ModerationEngine(); // fresh state for each test
    }

    // ?? Clean content ?????????????????????????????????????????????????

    @Test
    @DisplayName("Clean content -> ALLOW")
    void testCleanContentIsAllowed() {
        ModerationResult result = engine.moderate("user1", "Hello, have a great day!", Context.GENERAL);
        assertEquals(ModerationAction.ALLOW, result.getAction(),
                "Clean content should be ALLOWED");
    }

    @Test
    @DisplayName("Clean content still produces an audit log entry")
    void testCleanContentLogsEntry() {
        engine.moderate("user1", "Good morning!", Context.GENERAL);
        assertEquals(1, engine.getAuditLogger().getTotalLogCount(),
                "Even clean content must produce a log entry");
    }

    @Test
    @DisplayName("Clean content result.isClean() returns true")
    void testCleanContentIsCleanFlag() {
        ModerationResult result = engine.moderate("u0", "Nice weather today!", Context.GENERAL);
        assertTrue(result.isClean(), "isClean() should be true for clean content");
    }

    // ?? Severity-based actions ????????????????????????????????????????

    @Test
    @DisplayName("HIGH severity word -> BLOCK")
    void testHighSeverityWordIsBlocked() {
        ModerationResult result = engine.moderate("user2", "I will kill you!", Context.GENERAL);
        assertEquals(ModerationAction.BLOCK, result.getAction(),
                "HIGH severity word should result in BLOCK");
    }

    @Test
    @DisplayName("MEDIUM severity word -> not ALLOW")
    void testMediumSeverityWordIsFlagged() {
        ModerationResult result = engine.moderate("userX", "I enjoy violence", Context.GENERAL);
        assertNotEquals(ModerationAction.ALLOW, result.getAction(),
                "MEDIUM severity word should not be ALLOWED");
    }

    @Test
    @DisplayName("LOW severity word (1st strike) -> ALLOW_WITH_WARNING")
    void testLowSeverityWordAllowsWithWarning() {
        ModerationResult result = engine.moderate("user3", "You are stupid", Context.GENERAL);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, result.getAction(),
                "LOW severity first offence should be ALLOW_WITH_WARNING");
    }

    // ?? Text normalization / evasion detection ?????????????????????????

    @Test
    @DisplayName("Leet-speak 'k1ll' is detected as 'kill'")
    void testLeetSpeakEvasionIsDetected() {
        ModerationResult result = engine.moderate("user4", "I will k1ll you", Context.GENERAL);
        assertNotEquals(ModerationAction.ALLOW, result.getAction(),
                "Leet-speak 'k1ll' should be detected");
        assertEquals("kill", result.getTriggeredWord(),
                "Triggered word should be normalised to 'kill'");
    }

    @Test
    @DisplayName("Symbol evasion 'b0mb' is detected as 'bomb'")
    void testSymbolEvasionIsDetected() {
        ModerationResult result = engine.moderate("user5", "b0mb threat", Context.GENERAL);
        assertNotNull(result.getTriggeredWord(), "Symbol evasion should be detected");
        assertEquals("bomb", result.getTriggeredWord(),
                "Triggered word should be normalised to 'bomb'");
    }

    @Test
    @DisplayName("Result contains normalised text field")
    void testResultContainsNormalizedText() {
        ModerationResult result = engine.moderate("u1", "B0MB attack!", Context.GENERAL);
        assertNotNull(result.getNormalizedText(), "Normalised text must not be null");
        assertTrue(result.getNormalizedText().contains("bomb"),
                "Normalised text should contain the decoded word 'bomb'");
    }

    // ?? Strike escalation (full pipeline) ????????????????????????????

    @Test
    @DisplayName("Strike escalation: 1st=WARNING, 2nd=TEMP_BLOCK, 3rd=PERM_BLOCK")
    void testStrikeEscalationProgression() {
        // 1st offence with LOW word
        ModerationResult r1 = engine.moderate("repeat_user", "You are an idiot", Context.GENERAL);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, r1.getAction(),
                "1st strike should produce ALLOW_WITH_WARNING");

        // 2nd offence
        ModerationResult r2 = engine.moderate("repeat_user", "What a loser", Context.GENERAL);
        assertEquals(ModerationAction.TEMPORARY_BLOCK, r2.getAction(),
                "2nd strike should produce TEMPORARY_BLOCK");

        // 3rd offence
        ModerationResult r3 = engine.moderate("repeat_user", "Stop being dumb", Context.GENERAL);
        assertEquals(ModerationAction.PERMANENT_BLOCK, r3.getAction(),
                "3rd strike should produce PERMANENT_BLOCK");
    }

    // ?? Context overrides ????????????????????????????????????????????

    @Test
    @DisplayName("EDUCATIONAL context: 'kill' -> ALLOW_WITH_WARNING")
    void testEducationalContextAllowsKillWord() {
        ModerationResult result = engine.moderate("student1",
                "The causes of kill in war zones", Context.EDUCATIONAL);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, result.getAction(),
                "EDUCATIONAL context should allow 'kill' with a warning");
    }

    @Test
    @DisplayName("MEDICAL context: 'drug' -> ALLOW_WITH_WARNING")
    void testMedicalContextAllowsDrug() {
        ModerationResult result = engine.moderate("doctor1",
                "The patient was prescribed a drug", Context.MEDICAL);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, result.getAction(),
                "MEDICAL context should allow 'drug' with a warning");
    }

    @Test
    @DisplayName("GAMING context: 'cheat' -> BLOCK")
    void testGamingContextBlocksCheat() {
        ModerationResult result = engine.moderate("gamer1",
                "I know how to cheat in this game", Context.GAMING);
        assertEquals(ModerationAction.BLOCK, result.getAction(),
                "GAMING context should BLOCK 'cheat'");
    }

    // ?? Audit log integration ?????????????????????????????????????????

    @Test
    @DisplayName("Audit log records correct user ID")
    void testAuditLogRecordsCorrectUser() {
        engine.moderate("audit_user", "You are stupid", Context.GENERAL);
        String loggedUser = engine.getAuditLogger().getAllLogs().get(0).getUserId();
        assertEquals("audit_user", loggedUser,
                "Audit log must record the correct user ID");
    }

    @Test
    @DisplayName("Audit log count matches number of moderate() calls")
    void testAuditLogCountMatchesActions() {
        engine.moderate("u1", "Hello world",    Context.GENERAL);
        engine.moderate("u2", "I will kill",    Context.GENERAL);
        engine.moderate("u3", "You are stupid", Context.GENERAL);
        assertEquals(3, engine.getAuditLogger().getTotalLogCount(),
                "Audit log count must equal the number of moderation calls");
    }

    @Test
    @DisplayName("Audit log records triggered word")
    void testAuditLogRecordsTriggeredWord() {
        engine.moderate("u1", "Don't be an idiot", Context.GENERAL);
        String triggered = engine.getAuditLogger().getAllLogs().get(0).getTriggeredWord();
        assertEquals("idiot", triggered,
                "Audit log must record the exact triggered word");
    }

    // ?? Result completeness ??????????????????????????????????????????

    @Test
    @DisplayName("Result has non-null reason string")
    void testResultHasReason() {
        ModerationResult result = engine.moderate("u1", "I will bomb you", Context.GENERAL);
        assertNotNull(result.getReason(), "ModerationResult must include a reason");
        assertFalse(result.getReason().isEmpty(), "Reason must not be empty");
    }

    @Test
    @DisplayName("Result for clean content has null triggeredWord")
    void testCleanResultHasNullTriggeredWord() {
        ModerationResult result = engine.moderate("u1", "Nice day", Context.GENERAL);
        assertNull(result.getTriggeredWord(),
                "Clean result must have null triggeredWord");
    }
}
