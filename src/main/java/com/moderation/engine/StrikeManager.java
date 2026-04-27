package com.moderation.engine;

import com.moderation.model.ModerationAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * MODULE : Strike Manager
 * PERSON : Person 3 - Strike & Context Rule Manager
 * BRANCH : feature-strikes-context
 * ============================================================
 *
 * Tracks the cumulative number of violations per user and
 * escalates the moderation action accordingly.
 *
 * Escalation table:
 * ??????????????????????????????????
 * ? Strike ? Action                ?
 * ??????????????????????????????????
 * ?  1st   ? ALLOW_WITH_WARNING    ?
 * ?  2nd   ? TEMPORARY_BLOCK       ?
 * ?  3rd+  ? PERMANENT_BLOCK       ?
 * ??????????????????????????????????
 *
 * Each user's strike count is stored in an in-memory map.
 * Strikes persist for the lifetime of the ModerationEngine instance.
 */
public class StrikeManager {

    /** userId -> number of strikes accumulated */
    private final Map<String, Integer> strikeMap = new HashMap<>();

    // ------------------------------------------------------------------
    //  Core strike operations
    // ------------------------------------------------------------------

    /**
     * Increments the strike counter for {@code userId} and returns the
     * escalated action for their new strike count.
     *
     * @param userId The user who triggered a violation
     * @return The ModerationAction based on the updated strike count
     */
    public ModerationAction addStrikeAndGetAction(String userId) {
        int current = strikeMap.getOrDefault(userId, 0);
        current++;
        strikeMap.put(userId, current);
        return actionForCount(current);
    }

    /**
     * Returns the current strike count for a user (0 if they have none).
     */
    public int getStrikeCount(String userId) {
        return strikeMap.getOrDefault(userId, 0);
    }

    /**
     * Resets the strike count for a user to zero (e.g., after an appeal).
     */
    public void resetStrikes(String userId) {
        strikeMap.remove(userId);
    }

    /**
     * Returns what the next action WOULD be without incrementing the counter.
     * Useful for previewing consequences.
     */
    public ModerationAction peekNextAction(String userId) {
        int next = strikeMap.getOrDefault(userId, 0) + 1;
        return actionForCount(next);
    }

    /**
     * Returns a read-only snapshot of all user strike counts.
     * Useful for administration views.
     */
    public Map<String, Integer> getAllStrikes() {
        return Collections.unmodifiableMap(strikeMap);
    }

    /** Clears all strikes (useful for testing / admin reset). */
    public void resetAllStrikes() {
        strikeMap.clear();
    }

    // ------------------------------------------------------------------
    //  Internal helper
    // ------------------------------------------------------------------

    private ModerationAction actionForCount(int count) {
        if (count == 1) return ModerationAction.ALLOW_WITH_WARNING;
        if (count == 2) return ModerationAction.TEMPORARY_BLOCK;
        return          ModerationAction.PERMANENT_BLOCK;
    }
}
