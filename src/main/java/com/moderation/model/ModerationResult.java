package com.moderation.model;

/**
 * Immutable value object returned by the ModerationEngine for every request.
 *
 * Contains:
 *  - originalText   - the raw input from the user
 *  - normalizedText - the cleaned/decoded version used for matching
 *  - triggeredWord  - the specific banned word that caused the action (null if clean)
 *  - severity       - severity of the triggered word (null if clean)
 *  - action         - the final moderation decision
 *  - reason         - human-readable explanation of the decision
 *
 * Module Owner : Person 1 - Core Moderation & Severity Engine
 * Branch       : feature-severity-engine
 */
public class ModerationResult {

    private final String           originalText;
    private final String           normalizedText;
    private final String           triggeredWord;
    private final Severity         severity;
    private final ModerationAction action;
    private final String           reason;

    public ModerationResult(String originalText, String normalizedText,
                            String triggeredWord, Severity severity,
                            ModerationAction action, String reason) {
        this.originalText   = originalText;
        this.normalizedText = normalizedText;
        this.triggeredWord  = triggeredWord;
        this.severity       = severity;
        this.action         = action;
        this.reason         = reason;
    }

    public String           getOriginalText()   { return originalText; }
    public String           getNormalizedText()  { return normalizedText; }
    public String           getTriggeredWord()   { return triggeredWord; }
    public Severity         getSeverity()        { return severity; }
    public ModerationAction getAction()          { return action; }
    public String           getReason()          { return reason; }

    /** Returns true if the content was completely clean (no banned word matched). */
    public boolean isClean() { return triggeredWord == null; }

    @Override
    public String toString() {
        return String.format(
            "[ModerationResult] Action=%-20s | Severity=%-6s | Triggered='%s' | Reason=%s",
            action, severity, triggeredWord, reason);
    }
}
