package com.moderation;

import com.moderation.log.AuditLogger;
import com.moderation.model.AuditLog;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TEST MODULE : Audit Logger Tests
 * PERSON      : Person 4 - Audit Logs & Testing
 * BRANCH      : feature-audit-logs
 * ============================================================
 *
 * Verifies:
 *  - A log entry is created for every moderation action
 *  - Log counts are accurate across multiple events
 *  - Every field (user, word, action, severity, reason, timestamp) is stored correctly
 *  - Query methods (by user, by action, by severity) return correct subsets
 *  - clearLogs resets the count to zero
 *
 * Run with Maven: mvn test -pl . -Dtest=AuditLoggerTest
 */
@DisplayName("Person 4 - Audit Logger Tests")
public class AuditLoggerTest {

    private AuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new AuditLogger();
    }

    // ?? Creation ?????????????????????????????????????????????????????

    @Test
    @DisplayName("Fresh logger has 0 entries")
    void testEmptyLoggerReturnsZeroCount() {
        assertEquals(0, logger.getTotalLogCount(),
                "A newly created AuditLogger must have 0 entries");
    }

    @Test
    @DisplayName("One log call produces one entry")
    void testLogCreatedOnModerationAction() {
        logger.log("user1", "kill", ModerationAction.BLOCK, Severity.HIGH, "Test reason");
        assertEquals(1, logger.getTotalLogCount(),
                "One log() call should produce exactly one entry");
    }

    @Test
    @DisplayName("Three log calls produce exactly 3 entries")
    void testCorrectLogCount() {
        logger.log("user1", "kill",   ModerationAction.BLOCK,             Severity.HIGH,   "r1");
        logger.log("user2", "idiot",  ModerationAction.ALLOW_WITH_WARNING, Severity.LOW,   "r2");
        logger.log("user3", "weapon", ModerationAction.FLAG,              Severity.MEDIUM, "r3");
        assertEquals(3, logger.getTotalLogCount(),
                "Log count must match the number of log() calls");
    }

    // ?? Field accuracy ???????????????????????????????????????????????

    @Test
    @DisplayName("Log records correct user ID")
    void testLogContentAccuracy_UserId() {
        logger.log("alice", "bomb", ModerationAction.BLOCK, Severity.HIGH, "High severity word");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals("alice", log.getUserId(), "Stored user ID must match");
    }

    @Test
    @DisplayName("Log records correct triggered word")
    void testLogContentAccuracy_TriggeredWord() {
        logger.log("bob", "bomb", ModerationAction.BLOCK, Severity.HIGH, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals("bomb", log.getTriggeredWord(), "Stored triggered word must match");
    }

    @Test
    @DisplayName("Log records correct action")
    void testLogContentAccuracy_Action() {
        logger.log("carol", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, log.getAction(),
                "Stored action must match");
    }

    @Test
    @DisplayName("Log records correct severity")
    void testLogContentAccuracy_Severity() {
        logger.log("dave", "drug", ModerationAction.FLAG, Severity.MEDIUM, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(Severity.MEDIUM, log.getSeverity(), "Stored severity must match");
    }

    @Test
    @DisplayName("Log records correct reason string")
    void testLogContentAccuracy_Reason() {
        String reason = "User triggered a medium severity word";
        logger.log("eve", "drug", ModerationAction.FLAG, Severity.MEDIUM, reason);
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(reason, log.getReason(), "Stored reason must match");
    }

    @Test
    @DisplayName("Timestamp is non-null")
    void testTimestampIsNotNull() {
        logger.log("frank", "kill", ModerationAction.BLOCK, Severity.HIGH, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertNotNull(log.getTimestamp(), "Timestamp must not be null");
    }

    // ?? Query by user ????????????????????????????????????????????????

    @Test
    @DisplayName("getLogsForUser returns only that user's entries")
    void testGetLogsForUser() {
        logger.log("user1", "kill",  ModerationAction.BLOCK, Severity.HIGH, "r1");
        logger.log("user2", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r2");
        logger.log("user1", "bomb",  ModerationAction.BLOCK, Severity.HIGH, "r3");

        List<AuditLog> user1Logs = logger.getLogsForUser("user1");
        assertEquals(2, user1Logs.size(),
                "getLogsForUser should return exactly 2 entries for user1");
    }

    @Test
    @DisplayName("getLogsForUser returns empty list for unknown user")
    void testGetLogsForUnknownUser() {
        logger.log("user1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        List<AuditLog> result = logger.getLogsForUser("nobody");
        assertTrue(result.isEmpty(),
                "Unknown user should have an empty log list");
    }

    // ?? Query by action ??????????????????????????????????????????????

    @Test
    @DisplayName("getLogsByAction returns correct subset")
    void testGetLogsByAction() {
        logger.log("u1", "kill",  ModerationAction.BLOCK,             Severity.HIGH, "r");
        logger.log("u2", "bomb",  ModerationAction.BLOCK,             Severity.HIGH, "r");
        logger.log("u3", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r");

        List<AuditLog> blockLogs = logger.getLogsByAction(ModerationAction.BLOCK);
        assertEquals(2, blockLogs.size(),
                "getLogsByAction(BLOCK) should return exactly 2 entries");
    }

    // ?? Query by severity ????????????????????????????????????????????

    @Test
    @DisplayName("getLogsBySeverity returns correct subset")
    void testGetLogsBySeverity() {
        logger.log("u1", "kill",  ModerationAction.BLOCK, Severity.HIGH,   "r");
        logger.log("u2", "drug",  ModerationAction.FLAG,  Severity.MEDIUM, "r");
        logger.log("u3", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r");
        logger.log("u4", "bomb",  ModerationAction.BLOCK, Severity.HIGH,   "r");

        List<AuditLog> highLogs = logger.getLogsBySeverity(Severity.HIGH);
        assertEquals(2, highLogs.size(),
                "getLogsBySeverity(HIGH) should return 2 entries");
    }

    // ?? Clear / isEmpty ??????????????????????????????????????????????

    @Test
    @DisplayName("clearLogs resets count to 0")
    void testClearLogs() {
        logger.log("u1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        logger.clearLogs();
        assertEquals(0, logger.getTotalLogCount(),
                "After clearLogs, count must be 0");
    }

    @Test
    @DisplayName("isEmpty returns true on fresh logger")
    void testIsEmptyOnFreshLogger() {
        assertTrue(logger.isEmpty(),
                "Fresh logger must report isEmpty() == true");
    }

    @Test
    @DisplayName("isEmpty returns false after logging")
    void testIsEmptyAfterLogging() {
        logger.log("u1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        assertFalse(logger.isEmpty(),
                "Logger must report isEmpty() == false after at least one entry");
    }

    // ?? getAllLogs immutability ????????????????????????????????????????

    @Test
    @DisplayName("getAllLogs returns all entries in insertion order")
    void testGetAllLogsOrder() {
        logger.log("a", "kill",  ModerationAction.BLOCK, Severity.HIGH, "first");
        logger.log("b", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "second");

        List<AuditLog> all = logger.getAllLogs();
        assertEquals(2, all.size());
        assertEquals("a", all.get(0).getUserId(), "First entry should be user 'a'");
        assertEquals("b", all.get(1).getUserId(), "Second entry should be user 'b'");
    }
}
