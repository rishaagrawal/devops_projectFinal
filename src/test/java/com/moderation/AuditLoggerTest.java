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

@DisplayName("Person 4 - Audit Logger Tests")
public class AuditLoggerTest {

    private AuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new AuditLogger();
    }

    @Test
    @DisplayName("Fresh logger has 0 entries")
    void testEmptyLoggerReturnsZeroCount() {
        assertEquals(0, logger.getTotalLogCount());
    }

    @Test
    @DisplayName("One log call produces one entry")
    void testLogCreatedOnModerationAction() {
        logger.log("user1", "kill", ModerationAction.BLOCK, Severity.HIGH, "Test reason");
        assertEquals(1, logger.getTotalLogCount());
    }

    @Test
    @DisplayName("Three log calls produce exactly 3 entries")
    void testCorrectLogCount() {
        logger.log("user1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r1");
        logger.log("user2", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r2");
        logger.log("user3", "weapon", ModerationAction.FLAG, Severity.MEDIUM, "r3");
        assertEquals(3, logger.getTotalLogCount());
    }

    @Test
    @DisplayName("Log records correct user ID")
    void testLogContentAccuracy_UserId() {
        logger.log("alice", "bomb", ModerationAction.BLOCK, Severity.HIGH, "High severity word");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals("alice", log.getUserId());
    }

    @Test
    @DisplayName("Log records correct triggered word")
    void testLogContentAccuracy_TriggeredWord() {
        logger.log("bob", "bomb", ModerationAction.BLOCK, Severity.HIGH, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals("bomb", log.getTriggeredWord());
    }

    @Test
    @DisplayName("Log records correct action")
    void testLogContentAccuracy_Action() {
        logger.log("carol", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, log.getAction());
    }

    @Test
    @DisplayName("Log records correct severity")
    void testLogContentAccuracy_Severity() {
        logger.log("dave", "drug", ModerationAction.FLAG, Severity.MEDIUM, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(Severity.MEDIUM, log.getSeverity());
    }

    @Test
    @DisplayName("Log records correct reason string")
    void testLogContentAccuracy_Reason() {
        String reason = "User triggered a medium severity word";
        logger.log("eve", "drug", ModerationAction.FLAG, Severity.MEDIUM, reason);
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(reason, log.getReason());
    }

    @Test
    @DisplayName("Timestamp is non-null")
    void testTimestampIsNotNull() {
        logger.log("frank", "kill", ModerationAction.BLOCK, Severity.HIGH, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertNotNull(log.getTimestamp());
    }

    @Test
    @DisplayName("getLogsForUser returns correct entries")
    void testGetLogsForUser() {
        logger.log("user1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r1");
        logger.log("user2", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r2");
        logger.log("user1", "bomb", ModerationAction.BLOCK, Severity.HIGH, "r3");

        List<AuditLog> user1Logs = logger.getLogsForUser("user1");
        assertEquals(2, user1Logs.size());
    }

    @Test
    @DisplayName("getLogsByAction returns correct subset")
    void testGetLogsByAction() {
        logger.log("u1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        logger.log("u2", "bomb", ModerationAction.BLOCK, Severity.HIGH, "r");
        logger.log("u3", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r");

        List<AuditLog> blockLogs = logger.getLogsByAction(ModerationAction.BLOCK);
        assertEquals(2, blockLogs.size());
    }

    @Test
    @DisplayName("getLogsBySeverity returns correct subset")
    void testGetLogsBySeverity() {
        logger.log("u1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        logger.log("u2", "drug", ModerationAction.FLAG, Severity.MEDIUM, "r");
        logger.log("u3", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r");
        logger.log("u4", "bomb", ModerationAction.BLOCK, Severity.HIGH, "r");

        List<AuditLog> highLogs = logger.getLogsBySeverity(Severity.HIGH);
        assertEquals(2, highLogs.size());
    }

    @Test
    @DisplayName("clearLogs resets count to 0")
    void testClearLogs() {
        logger.log("u1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        logger.clearLogs();
        assertEquals(0, logger.getTotalLogCount());
    }
}