package com.moderation.log;

import com.moderation.model.AuditLog;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Audit Logger
 * Maintains an in-memory list of AuditLog entries.
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

    /** Returns all logs (unmodifiable). */
    public List<AuditLog> getAllLogs() {
        return Collections.unmodifiableList(logs);
    }

    /** Returns logs for a specific user. */
    public List<AuditLog> getLogsForUser(String userId) {
        return logs.stream()
                .filter(l -> l.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /** Returns logs filtered by action. */
    public List<AuditLog> getLogsByAction(ModerationAction action) {
        return logs.stream()
                .filter(l -> l.getAction() == action)
                .collect(Collectors.toList());
    }

    /** Returns logs filtered by severity. */
    public List<AuditLog> getLogsBySeverity(Severity severity) {
        return logs.stream()
                .filter(l -> l.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /** Total number of logs. */
    public int getTotalLogCount() {
        return logs.size();
    }

    /** Check if empty. */
    public boolean isEmpty() {
        return logs.isEmpty();
    }

    /** Print all logs. */
    public void printAll() {
        if (logs.isEmpty()) {
            System.out.println("No logs recorded.");
            return;
        }
        System.out.println("===== AUDIT LOG =====");
        logs.forEach(System.out::println);
        System.out.println("=====================");
    }

    /** Clear logs (for testing). */
    public void clearLogs() {
        logs.clear();
    }
}