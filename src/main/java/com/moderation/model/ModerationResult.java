package com.moderation.model;

public class ModerationResult {

    private final String originalText;
    private final String normalizedText;
    private final String triggeredWord;
    private final Severity severity;
    private final ModerationAction action;
    private final String reason;

    public ModerationResult(String originalText, String normalizedText,
                            String triggeredWord, Severity severity,
                            ModerationAction action, String reason) {
        this.originalText = originalText;
        this.normalizedText = normalizedText;
        this.triggeredWord = triggeredWord;
        this.severity = severity;
        this.action = action;
        this.reason = reason;
    }

    public String getOriginalText()   { return originalText; }
    public String getNormalizedText() { return normalizedText; }
    public String getTriggeredWord()  { return triggeredWord; }
    public Severity getSeverity()     { return severity; }
    public ModerationAction getAction() { return action; }
    public String getReason()         { return reason; }

    @Override
    public String toString() {
        return String.format("[ModerationResult] Action=%s | Severity=%s | Triggered='%s' | Reason=%s",
                action, severity, triggeredWord, reason);
    }
}
