package com.moderation.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.moderation.model.AuditLog;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;

/**
 * ============================================================
 * MODULE : Audit Logger
 * PERSON : Person 4 - Audit Logs & Testing
 * BRANCH : feature-audit-logs
 * ============================================================
 *
 * Maintains an in-memory, append-only list of AuditLog entries.
 * Every moderation decision (clean or violation) must be recorded here
 * to provide a full accountability trail.
 *
 * Features:
 *  - Log every event with user, word, action, severity, reason, timestamp
 *  - Query all logs, logs by user, logs by action, or logs by severity
 *  - Print formatted summary to console
 *  - Export summary statistics
 *  - Clear all logs (for testing / reset)
 */
public class AuditLogger {

    private final List<AuditLog> logs = new ArrayList<>();

    // ------------------------------------------------------------------
    //  Write
    // ------------------------------------------------------------------

    /**
     * Records a new moderation event.
     *
     * @param userId        The user who sent the content
     * @param triggeredWord The banned word that was matched ("none" if clean)
     * @param action        The final moderation action applied
     * @param severity      The severity of the triggered word (null if clean)
     * @param reason        A human-readable explanation
     */
    public void log(String userId, String triggeredWord,
                    ModerationAction action, Severity severity, String reason) {
        AuditLog entry = new AuditLog(userId, triggeredWord, action, severity, reason);
        logs.add(entry);
    }

    // ------------------------------------------------------------------
    //  Read / Query
    // ------------------------------------------------------------------

    /** Returns an unmodifiable view of ALL log entries, oldest first. */
    public List<AuditLog> getAllLogs() {
        return Collections.unmodifiableList(logs);
    }

    /** Returns all logs belonging to a specific user. */
    public List<AuditLog> getLogsForUser(String userId) {
        return logs.stream()
                .filter(l -> l.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /** Returns all logs where the final action matches the given value. */
    public List<AuditLog> getLogsByAction(ModerationAction action) {
        return logs.stream()
                .filter(l -> l.getAction() == action)
                .collect(Collectors.toList());
    }

    /** Returns all logs where the severity matches the given value. */
    public List<AuditLog> getLogsBySeverity(Severity severity) {
        return logs.stream()
                .filter(l -> l.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /** Returns the total number of recorded events. */
    public int getTotalLogCount() {
        return logs.size();
    }

    /** Returns true if there are no log entries. */
    public boolean isEmpty() {
        return logs.isEmpty();
    }

    // ------------------------------------------------------------------
    //  Display
    // ------------------------------------------------------------------

    /**
     * Prints all log entries to standard output in a formatted table.
     * Called from the menu-driven interface (menu option: 'View Audit Log').
     */
    public void printAll() {
        if (logs.isEmpty()) {
            System.out.println("  No events recorded yet.");
            return;
        }
        String sep = "=".repeat(55);
        String line = "-".repeat(55);
        System.out.println("  " + sep);
        System.out.println("  AUDIT LOG  (" + logs.size() + " entries)");
        System.out.println("  " + line);
        for (AuditLog log : logs) {
            System.out.println("  " + log.toString());
        }
        System.out.println("  " + sep);
        System.out.println();
    }

    /**
     * Prints a summary: counts grouped by action type.
     */
    public void printSummary() {
        String sep  = "=".repeat(55);
        String line = "-".repeat(55);
        System.out.println();
        System.out.println("  " + sep);
        System.out.println("  AUDIT SUMMARY");
        System.out.println("  " + line);
        for (ModerationAction action : ModerationAction.values()) {
            long count = logs.stream().filter(l -> l.getAction() == action).count();
            if (count > 0) {
                System.out.printf("  %-24s : %d%n", action, count);
            }
        }
        System.out.println("  " + line);
        System.out.printf("  %-24s : %d%n", "TOTAL", logs.size());
        System.out.println("  " + sep);
        System.out.println();
    }

    // ------------------------------------------------------------------
    //  Maintenance
    // ------------------------------------------------------------------

    /** Clears all log entries (useful for testing). */
    public void clearLogs() {
        logs.clear();
    }
}
