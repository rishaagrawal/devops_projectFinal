package com.moderation.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record of a single moderation event.
 *
 * Captures:
 *  - userId
 *  - triggeredWord
 *  - action
 *  - severity
 *  - reason
 *  - timestamp
 */
public class AuditLog {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String userId;
    private final String triggeredWord;
    private final ModerationAction action;
    private final Severity severity;
    private final String reason;
    private final LocalDateTime timestamp;

    public AuditLog(String userId, String triggeredWord,
                    ModerationAction action, Severity severity, String reason) {
        this.userId = userId;
        this.triggeredWord = triggeredWord;
        this.action = action;
        this.severity = severity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    public String getUserId() { return userId; }
    public String getTriggeredWord() { return triggeredWord; }
    public ModerationAction getAction() { return action; }
    public Severity getSeverity() { return severity; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format(
                "[%s] User='%s' | Word='%s' | Severity=%s | Action=%s | Reason=%s",
                timestamp.format(FORMATTER),
                userId,
                triggeredWord,
                severity,
                action,
                reason
        );
    }
}