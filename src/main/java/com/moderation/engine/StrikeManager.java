package com.moderation.engine;

import com.moderation.model.ModerationAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks strike counts per user and escalates moderation actions:
 *
 * 1st strike → warning (ALLOW_WITH_WARNING)
 * 2nd strike → temporary block (TEMPORARY_BLOCK)
 * 3rd+ strike → permanent block (PERMANENT_BLOCK)
 */
public class StrikeManager {

    private final Map<String, Integer> strikeMap = new HashMap<>();

    /**
     * Adds a strike for the user and returns the appropriate escalated action.
     */
    public ModerationAction addStrikeAndGetAction(String userId) {
        int current = strikeMap.getOrDefault(userId, 0);
        current++;
        strikeMap.put(userId, current);
        return getActionForStrikes(current);
    }

    /**
     * Returns current strike count for a user.
     */
    public int getStrikeCount(String userId) {
        return strikeMap.getOrDefault(userId, 0);
    }

    /**
     * Resets strikes for a user (e.g., after appeal).
     */
    public void resetStrikes(String userId) {
        strikeMap.remove(userId);
    }

    /**
     * Peek at what action would be assigned without incrementing.
     */
    public ModerationAction peekNextAction(String userId) {
        int next = strikeMap.getOrDefault(userId, 0) + 1;
        return getActionForStrikes(next);
    }

    private ModerationAction getActionForStrikes(int count) {
        if (count == 1) return ModerationAction.ALLOW_WITH_WARNING;
        if (count == 2) return ModerationAction.TEMPORARY_BLOCK;
        return ModerationAction.PERMANENT_BLOCK;
    }
}
