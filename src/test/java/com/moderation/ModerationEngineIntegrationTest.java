package com.moderation;

import com.moderation.engine.ModerationEngine;
import com.moderation.model.Context;
import com.moderation.model.ModerationAction;
import com.moderation.model.ModerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests covering the full moderation pipeline:
 * Text Matching → Severity → Context + Strikes → Audit Log
 */
public class ModerationEngineIntegrationTest {

    private ModerationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ModerationEngine();
    }

    // ---- Clean Content ----

    @Test
    void testCleanContentIsAllowed() {
        ModerationResult result = engine.moderate("user1", "Hello, have a great day!", Context.GENERAL);
        assertEquals(ModerationAction.ALLOW, result.getAction(),
                "Clean content should be ALLOWED");
    }

    @Test
    void testCleanContentLogsEntry() {
        engine.moderate("user1", "Good morning!", Context.GENERAL);
        assertEquals(1, engine.getAuditLogger().getTotalLogCount(),
                "Even clean content should produce a log entry");
    }

    // ---- Severity-based Actions ----

    @Test
    void testHighSeverityWordIsBlocked() {
        ModerationResult result = engine.moderate("user2", "I will kill you!", Context.GENERAL);
        assertEquals(ModerationAction.BLOCK, result.getAction(),
                "HIGH severity word should result in BLOCK");
    }

    @Test
    void testMediumSeverityWordIsFlagged() {
        ModerationResult result = engine.moderate("userX", "I love watching violence", Context.GENERAL);
        // First strike on medium escalates; check it is at least FLAG
        assertNotEquals(ModerationAction.ALLOW, result.getAction(),
                "MEDIUM severity word should not be allowed");
    }

    @Test
    void testLowSeverityWordAllowsWithWarning() {
        ModerationResult result = engine.moderate("user3", "You are stupid", Context.GENERAL);
        // 1st strike + LOW severity = ALLOW_WITH_WARNING
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, result.getAction(),
                "LOW severity first offence should be ALLOW_WITH_WARNING");
    }

    // ---- Leet-speak Evasion ----

    @Test
    void testLeetSpeakEvasionIsDetected() {
        ModerationResult result = engine.moderate("user4", "I will k1ll you", Context.GENERAL);
        assertNotEquals(ModerationAction.ALLOW, result.getAction(),
                "Leet-speak evasion should be detected and actioned");
        assertEquals("kill", result.getTriggeredWord(),
                "Triggered word should be the normalized 'kill'");
    }

    @Test
    void testSymbolEvasionIsDetected() {
        ModerationResult result = engine.moderate("user5", "b0mb threat", Context.GENERAL);
        assertNotNull(result.getTriggeredWord(), "Symbol evasion should be detected");
        assertEquals("bomb", result.getTriggeredWord());
    }

    // ---- Strike Escalation (full pipeline) ----

    @Test
    void testStrikeEscalationProgression() {
        // Offence 1 → ALLOW_WITH_WARNING (LOW severity word, 1st strike)
        ModerationResult r1 = engine.moderate("repeat_user", "You are an idiot", Context.GENERAL);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, r1.getAction());

        // Offence 2 → TEMPORARY_BLOCK
        ModerationResult r2 = engine.moderate("repeat_user", "What a loser", Context.GENERAL);
        assertEquals(ModerationAction.TEMPORARY_BLOCK, r2.getAction());

        // Offence 3 → PERMANENT_BLOCK
        ModerationResult r3 = engine.moderate("repeat_user", "Stop being dumb", Context.GENERAL);
        assertEquals(ModerationAction.PERMANENT_BLOCK, r3.getAction());
    }

    // ---- Context Overrides ----

    @Test
    void testEducationalContextAllowsKillWord() {
        ModerationResult result = engine.moderate("student1",
                "The causes of kill in war zones", Context.EDUCATIONAL);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, result.getAction(),
                "EDUCATIONAL context should allow 'kill' with warning");
    }

    @Test
    void testMedicalContextAllowsDrug() {
        ModerationResult result = engine.moderate("doctor1",
                "The patient was prescribed a drug", Context.MEDICAL);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, result.getAction(),
                "MEDICAL context should allow 'drug' with warning");
    }

    @Test
    void testGamingContextBlocksCheat() {
        ModerationResult result = engine.moderate("gamer1",
                "I know how to cheat in this game", Context.GAMING);
        assertEquals(ModerationAction.BLOCK, result.getAction(),
                "GAMING context should BLOCK 'cheat'");
    }

    // ---- Audit Log Integration ----

    @Test
    void testAuditLogRecordsCorrectUser() {
        engine.moderate("audit_user", "You are stupid", Context.GENERAL);
        String loggedUser = engine.getAuditLogger().getAllLogs().get(0).getUserId();
        assertEquals("audit_user", loggedUser,
                "Audit log should record correct user ID");
    }

    @Test
    void testAuditLogCountMatchesActions() {
        engine.moderate("u1", "Hello world",   Context.GENERAL);
        engine.moderate("u2", "I will kill",   Context.GENERAL);
        engine.moderate("u3", "You are stupid", Context.GENERAL);
        assertEquals(3, engine.getAuditLogger().getTotalLogCount(),
                "Audit log count should match number of moderation calls");
    }

    @Test
    void testAuditLogRecordsTriggeredWord() {
        engine.moderate("u1", "Don't be an idiot", Context.GENERAL);
        String triggered = engine.getAuditLogger().getAllLogs().get(0).getTriggeredWord();
        assertEquals("idiot", triggered, "Audit log should record the triggered word");
    }

    @Test
    void testResultContainsNormalizedText() {
        ModerationResult result = engine.moderate("u1", "B0MB attack!", Context.GENERAL);
        assertNotNull(result.getNormalizedText(),
                "Result should contain normalized text");
        assertTrue(result.getNormalizedText().contains("bomb"),
                "Normalized text should contain the decoded word");
    }
}
