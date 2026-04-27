package com.moderation;

import com.moderation.log.AuditLogger;
import com.moderation.model.AuditLog;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuditLoggerTest {

    private AuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new AuditLogger();
    }

    @Test
    void testLogCreatedOnModerationAction() {
        logger.log("user1", "kill", ModerationAction.BLOCK, Severity.HIGH, "Test reason");
        assertEquals(1, logger.getTotalLogCount(),
                "One log entry should be created after one moderation action");
    }

    @Test
    void testCorrectLogCount() {
        logger.log("user1", "kill",   ModerationAction.BLOCK,            Severity.HIGH,   "Test 1");
        logger.log("user2", "idiot",  ModerationAction.ALLOW_WITH_WARNING, Severity.LOW,  "Test 2");
        logger.log("user3", "weapon", ModerationAction.FLAG,             Severity.MEDIUM, "Test 3");
        assertEquals(3, logger.getTotalLogCount(),
                "Log count should match number of logged events");
    }

    @Test
    void testLogContentAccuracy_UserId() {
        logger.log("alice", "bomb", ModerationAction.BLOCK, Severity.HIGH, "High severity word");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals("alice", log.getUserId(), "User ID should match");
    }

    @Test
    void testLogContentAccuracy_TriggeredWord() {
        logger.log("bob", "bomb", ModerationAction.BLOCK, Severity.HIGH, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals("bomb", log.getTriggeredWord(), "Triggered word should match");
    }

    @Test
    void testLogContentAccuracy_Action() {
        logger.log("carol", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, log.getAction(),
                "Logged action should match");
    }

    @Test
    void testLogContentAccuracy_Severity() {
        logger.log("dave", "drug", ModerationAction.FLAG, Severity.MEDIUM, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(Severity.MEDIUM, log.getSeverity(), "Logged severity should match");
    }

    @Test
    void testLogContentAccuracy_Reason() {
        String reason = "User triggered medium severity word";
        logger.log("eve", "drug", ModerationAction.FLAG, Severity.MEDIUM, reason);
        AuditLog log = logger.getAllLogs().get(0);
        assertEquals(reason, log.getReason(), "Logged reason should match");
    }

    @Test
    void testTimestampIsNotNull() {
        logger.log("frank", "kill", ModerationAction.BLOCK, Severity.HIGH, "Reason");
        AuditLog log = logger.getAllLogs().get(0);
        assertNotNull(log.getTimestamp(), "Timestamp should not be null");
    }

    @Test
    void testGetLogsForUser() {
        logger.log("user1", "kill",  ModerationAction.BLOCK, Severity.HIGH, "r1");
        logger.log("user2", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r2");
        logger.log("user1", "bomb",  ModerationAction.BLOCK, Severity.HIGH, "r3");

        List<AuditLog> user1Logs = logger.getLogsForUser("user1");
        assertEquals(2, user1Logs.size(), "Should return 2 logs for user1");
    }

    @Test
    void testGetLogsByAction() {
        logger.log("u1", "kill",  ModerationAction.BLOCK,            Severity.HIGH, "r");
        logger.log("u2", "bomb",  ModerationAction.BLOCK,            Severity.HIGH, "r");
        logger.log("u3", "idiot", ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r");

        List<AuditLog> blockLogs = logger.getLogsByAction(ModerationAction.BLOCK);
        assertEquals(2, blockLogs.size(), "Should return 2 BLOCK logs");
    }

    @Test
    void testClearLogs() {
        logger.log("u1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        logger.clearLogs();
        assertEquals(0, logger.getTotalLogCount(), "Log count should be 0 after clear");
    }

    @Test
    void testEmptyLoggerReturnsZeroCount() {
        assertEquals(0, logger.getTotalLogCount(),
                "Fresh logger should have 0 entries");
    }
}
