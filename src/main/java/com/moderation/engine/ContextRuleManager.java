package com.moderation.engine;

import com.moderation.model.BannedWord;
import com.moderation.model.Context;
import com.moderation.model.ModerationAction;

import java.util.*;

/**
 * ============================================================
 * MODULE : Context Rule Manager
 * PERSON : Person 3 - Strike & Context Rule Manager
 * BRANCH : feature-strikes-context
 * ============================================================
 *
 * Applies context-specific overrides to moderation decisions so that
 * legitimate uses of sensitive words in academic, medical, or gaming
 * environments are handled appropriately.
 *
 * Override table:
 * ??????????????????????????????????????????????????????????????????????????
 * ? EDUCATIONAL ? kill, death, drug, suicide, violence, war, weapon, bomb  ?
 * ?             ? -> downgraded to ALLOW_WITH_WARNING                       ?
 * ??????????????????????????????????????????????????????????????????????????
 * ? MEDICAL     ? drug, overdose, narcotic, injection, suicide, death,     ?
 * ?             ? poison -> downgraded to ALLOW_WITH_WARNING                ?
 * ??????????????????????????????????????????????????????????????????????????
 * ? GAMING      ? cheat, hack, exploit, glitch -> upgraded to BLOCK         ?
 * ??????????????????????????????????????????????????????????????????????????
 * ? GENERAL     ? No override - standard severity rules apply              ?
 * ??????????????????????????????????????????????????????????????????????????
 */
public class ContextRuleManager {

    /** Words whitelisted in EDUCATIONAL contexts */
    private static final Set<String> EDUCATIONAL_WHITELIST = new HashSet<>(Arrays.asList(
            "kill", "death", "drug", "suicide", "violence", "war", "weapon", "bomb", "murder"
    ));

    /** Words whitelisted in MEDICAL contexts */
    private static final Set<String> MEDICAL_WHITELIST = new HashSet<>(Arrays.asList(
            "drug", "overdose", "narcotic", "injection", "suicide", "death", "poison"
    ));

    /** Extra-blocked words per context (always results in BLOCK) */
    private static final Map<Context, Set<String>> CONTEXT_BLOCKLIST = new HashMap<>();

    static {
        CONTEXT_BLOCKLIST.put(Context.GAMING,
                new HashSet<>(Arrays.asList("cheat", "hack", "exploit", "glitch")));
    }

    // ------------------------------------------------------------------
    //  Public API
    // ------------------------------------------------------------------

    /**
     * Returns an overriding ModerationAction based on the context,
     * or {@code null} if no override applies (caller should use default).
     *
     * @param context       The platform context
     * @param bannedWord    The matched banned word
     * @param defaultAction The action that would normally be applied
     * @return Override action, or null if no override
     */
    public ModerationAction getOverride(Context context, BannedWord bannedWord,
                                        ModerationAction defaultAction) {
        if (context == null || bannedWord == null) return null;

        String word = bannedWord.getWord();

        // Check whitelists (downgrade action)
        if (context == Context.EDUCATIONAL && EDUCATIONAL_WHITELIST.contains(word)) {
            return ModerationAction.ALLOW_WITH_WARNING;
        }
        if (context == Context.MEDICAL && MEDICAL_WHITELIST.contains(word)) {
            return ModerationAction.ALLOW_WITH_WARNING;
        }

        // Check context-specific extra blocklist (upgrade action)
        Set<String> extraBlocked = CONTEXT_BLOCKLIST.get(context);
        if (extraBlocked != null && extraBlocked.contains(word)) {
            return ModerationAction.BLOCK;
        }

        return null; // no override -> caller uses defaultAction
    }

    /**
     * Resolves the final action by applying any context override.
     * If no override exists the {@code defaultAction} is returned unchanged.
     */
    public ModerationAction resolveAction(Context context, BannedWord bannedWord,
                                          ModerationAction defaultAction) {
        ModerationAction override = getOverride(context, bannedWord, defaultAction);
        return (override != null) ? override : defaultAction;
    }

    /** Returns the whitelist for a given context (for display/admin). */
    public Set<String> getWhitelistForContext(Context context) {
        if (context == Context.EDUCATIONAL) return Collections.unmodifiableSet(EDUCATIONAL_WHITELIST);
        if (context == Context.MEDICAL)     return Collections.unmodifiableSet(MEDICAL_WHITELIST);
        return Collections.emptySet();
    }

    /** Returns the extra blocklist for a given context (for display/admin). */
    public Set<String> getBlocklistForContext(Context context) {
        Set<String> bl = CONTEXT_BLOCKLIST.get(context);
        return bl != null ? Collections.unmodifiableSet(bl) : Collections.emptySet();
    }
}

