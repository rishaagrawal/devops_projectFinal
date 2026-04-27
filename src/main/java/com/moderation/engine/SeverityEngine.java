package com.moderation.engine;

import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;

/**
 * ============================================================
 * MODULE : Severity Engine
 * PERSON : Person 1 - Core Moderation & Severity Engine
 * BRANCH : feature-severity-engine
 * ============================================================
 *
 * Maps a word's Severity classification to the appropriate base
 * ModerationAction.
 *
 * Severity -> Action table:
 * ???????????????????????????????????
 * ? HIGH     ? BLOCK                ?
 * ? MEDIUM   ? FLAG                 ?
 * ? LOW      ? ALLOW_WITH_WARNING   ?
 * ? null     ? ALLOW                ?
 * ???????????????????????????????????
 *
 * This is the "default" action before context overrides or strike
 * escalation are applied.
 */
public class SeverityEngine {

    /**
     * Returns the base moderation action for a given severity level.
     *
     * @param severity The severity of the detected word (may be null for clean content)
     * @return The corresponding ModerationAction
     */
    public ModerationAction evaluate(Severity severity) {
        if (severity == null) {
            return ModerationAction.ALLOW;
        }
        switch (severity) {
            case HIGH:   return ModerationAction.BLOCK;
            case MEDIUM: return ModerationAction.FLAG;
            case LOW:    return ModerationAction.ALLOW_WITH_WARNING;
            default:     return ModerationAction.ALLOW;
        }
    }

    /**
     * Returns a human-readable description of what will happen for a given severity.
     *
     * @param severity The severity level to describe
     * @return A short explanation string
     */
    public String describeAction(Severity severity) {
        ModerationAction action = evaluate(severity);
        switch (action) {
            case BLOCK:
                return "Content BLOCKED - HIGH severity violation detected.";
            case FLAG:
                return "Content FLAGGED for review - MEDIUM severity violation detected.";
            case ALLOW_WITH_WARNING:
                return "Content ALLOWED WITH WARNING - LOW severity violation detected.";
            default:
                return "Content ALLOWED - no violations.";
        }
    }
}
<<<<<<< HEAD

=======
>>>>>>> origin/main
