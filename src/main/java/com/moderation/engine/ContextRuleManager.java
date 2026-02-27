package com.moderation.engine;

import com.moderation.model.BannedWord;
import com.moderation.model.Context;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Applies context-based overrides to moderation decisions.
 *
 * EDUCATIONAL context: allows certain MEDIUM-severity words that are legitimate
 *                      in an academic / study setting.
 * MEDICAL context:     allows certain HIGH-severity terms used in clinical discussion.
 * GENERAL / GAMING:    no overrides; standard rules apply.
 */
public class ContextRuleManager {

    // Words that are allowed in EDUCATIONAL context despite their severity
    private static final Set<String> EDUCATIONAL_WHITELIST = new HashSet<>(Arrays.asList(
            "kill", "death", "drug", "suicide", "violence", "war", "weapon", "bomb"
    ));

    // Words that are allowed in MEDICAL context
    private static final Set<String> MEDICAL_WHITELIST = new HashSet<>(Arrays.asList(
            "drug", "overdose", "narcotic", "injection", "suicide", "death", "poison"
    ));

    // Context-specific extra blocked words (e.g., gaming cheating terms in GAMING)
    private static final Map<Context, Set<String>> CONTEXT_BLOCKLIST = new HashMap<>();

    static {
        CONTEXT_BLOCKLIST.put(Context.GAMING,
                new HashSet<>(Arrays.asList("cheat", "hack", "exploit", "glitch")));
    }

    /**
     * Returns an overriding ModerationAction if the context allows or escalates it,
     * or null if no override applies (use default severity-based action).
     */
    public ModerationAction getOverride(Context context, BannedWord bannedWord, ModerationAction defaultAction) {
        if (context == null || bannedWord == null) return null;

        String word = bannedWord.getWord();
        Severity severity = bannedWord.getSeverity();

        // Check whitelist — allow the word in this context
        if (context == Context.EDUCATIONAL && EDUCATIONAL_WHITELIST.contains(word)) {
            return ModerationAction.ALLOW_WITH_WARNING; // allow but warn
        }
        if (context == Context.MEDICAL && MEDICAL_WHITELIST.contains(word)) {
            return ModerationAction.ALLOW_WITH_WARNING;
        }

        // Check context-specific extra blocklist
        Set<String> extraBlocked = CONTEXT_BLOCKLIST.get(context);
        if (extraBlocked != null && extraBlocked.contains(word)) {
            return ModerationAction.BLOCK; // always block in this context
        }

        return null; // no override
    }

    /**
     * Convenience: resolve final action considering context.
     */
    public ModerationAction resolveAction(Context context, BannedWord bannedWord, ModerationAction defaultAction) {
        ModerationAction override = getOverride(context, bannedWord, defaultAction);
        return (override != null) ? override : defaultAction;
    }
}
