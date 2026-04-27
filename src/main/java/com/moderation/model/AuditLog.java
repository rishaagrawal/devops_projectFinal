package com.moderation.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record of a single moderation event.
 *
 * Captures:
 *  - userId        - who sent the content
 *  - triggeredWord - which banned word was found (or "none" for clean content)
 *  - action        - the final moderation action applied
 *  - severity      - severity level of the triggered word
 *  - reason        - full explanation string
 *  - timestamp     - exact date/time of the event (auto-assigned on construction)
 *
 * Module Owner : Person 4 - Audit Logs & Testing
 * Branch       : feature-audit-logs
 */
public class AuditLog {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String           userId;
    private final String           triggeredWord;
    private final ModerationAction action;
    private final Severity         severity;
    private final String           reason;
    private final LocalDateTime    timestamp;

    public AuditLog(String userId, String triggeredWord,
                    ModerationAction action, Severity severity, String reason) {
        this.userId        = userId;
        this.triggeredWord = triggeredWord;
        this.action        = action;
        this.severity      = severity;
        this.reason        = reason;
        this.timestamp     = LocalDateTime.now();
    }

    public String           getUserId()        { return userId; }
    public String           getTriggeredWord() { return triggeredWord; }
    public ModerationAction getAction()        { return action; }
    public Severity         getSeverity()      { return severity; }
    public String           getReason()        { return reason; }
    public LocalDateTime    getTimestamp()     { return timestamp; }

    @Override
    public String toString() {
        return String.format(
            "[%s] User=%-12s | Word=%-12s | Severity=%-6s | Action=%-20s | %s",
            timestamp.format(FORMATTER),
            "'" + userId + "'",
            "'" + triggeredWord + "'",
            severity,
            action,
            reason);
    }
}
