package com.moderation.log;

import com.moderation.model.AuditLog;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maintains an in-memory list of AuditLog entries.
 * Records every moderation decision for accountability and review.
 */
public class AuditLogger {

    private final List<AuditLog> logs = new ArrayList<>();

    /**
     * Records a new moderation event.
     */
    public void log(String userId, String triggeredWord,
                    ModerationAction action, Severity severity, String reason) {
        AuditLog entry = new AuditLog(userId, triggeredWord, action, severity, reason);
        logs.add(entry);
    }

    /**
     * Returns all log entries (unmodifiable).
     */
    public List<AuditLog> getAllLogs() {
        return Collections.unmodifiableList(logs);
    }

    /**
     * Returns logs for a specific user.
     */
    public List<AuditLog> getLogsForUser(String userId) {
        return logs.stream()
                .filter(l -> l.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of logged events.
     */
    public int getTotalLogCount() {
        return logs.size();
    }

    /**
     * Returns logs filtered by action type.
     */
    public List<AuditLog> getLogsByAction(ModerationAction action) {
        return logs.stream()
                .filter(l -> l.getAction() == action)
                .collect(Collectors.toList());
    }

    /**
     * Clears all logs (useful for testing).
     */
    public void clearLogs() {
        logs.clear();
    }

    /**
     * Prints all logs to stdout.
     */
    public void printAll() {
        if (logs.isEmpty()) {
            System.out.println("[AuditLogger] No logs recorded.");
            return;
        }
        System.out.println("===== AUDIT LOG =====");
        logs.forEach(System.out::println);
        System.out.println("=====================");
    }
}
