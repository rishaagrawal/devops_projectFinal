package com.moderation.engine;

import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;

/**
 * Maps Severity levels to Moderation Actions.
 *
 * HIGH   → BLOCK
 * MEDIUM → FLAG
 * LOW    → ALLOW_WITH_WARNING
 */
public class SeverityEngine {

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

    public String describeAction(Severity severity) {
        ModerationAction action = evaluate(severity);
        switch (action) {
            case BLOCK:             return "Content blocked due to HIGH severity violation.";
            case FLAG:              return "Content flagged for review due to MEDIUM severity violation.";
            case ALLOW_WITH_WARNING:return "Content allowed with a warning due to LOW severity violation.";
            default:                return "Content allowed.";
        }
    }
}
