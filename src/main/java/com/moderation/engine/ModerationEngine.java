package com.moderation.engine;

import com.moderation.log.AuditLogger;
import com.moderation.model.BannedWord;
import com.moderation.model.Context;
import com.moderation.model.ModerationAction;
import com.moderation.model.ModerationResult;
import com.moderation.model.Severity;
import com.moderation.util.TextMatcher;
import com.moderation.util.TextNormalizer;

/**
 * Central orchestrator that connects:
 *  1. Text Matching  → finds offending word
 *  2. Severity Engine → decides base action
 *  3. Context + Strikes → may override or escalate
 *  4. Audit Logger  → records final decision
 */
public class ModerationEngine {

    private final WordRepository    wordRepo;
    private final SeverityEngine    severityEngine;
    private final StrikeManager     strikeManager;
    private final ContextRuleManager contextManager;
    private final AuditLogger       auditLogger;

    public ModerationEngine() {
        this.wordRepo        = new WordRepository();
        this.severityEngine  = new SeverityEngine();
        this.strikeManager   = new StrikeManager();
        this.contextManager  = new ContextRuleManager();
        this.auditLogger     = new AuditLogger();
    }

    // Constructor for dependency injection (testing)
    public ModerationEngine(WordRepository wordRepo, SeverityEngine severityEngine,
                            StrikeManager strikeManager, ContextRuleManager contextManager,
                            AuditLogger auditLogger) {
        this.wordRepo        = wordRepo;
        this.severityEngine  = severityEngine;
        this.strikeManager   = strikeManager;
        this.contextManager  = contextManager;
        this.auditLogger     = auditLogger;
    }

    /**
     * Moderate text for a given user and context.
     * Full pipeline: match → severity → context override → strike escalation → log.
     */
    public ModerationResult moderate(String userId, String text, Context context) {
        String normalized = TextNormalizer.normalize(text);

        // Step 1: Text matching
        BannedWord found = TextMatcher.findMatch(text, wordRepo.getBannedWords());

        if (found == null) {
            // Clean content
            ModerationResult result = new ModerationResult(
                    text, normalized, null, null, ModerationAction.ALLOW, "No violations found.");
            auditLogger.log(userId, "none", ModerationAction.ALLOW, null, "Clean content");
            return result;
        }

        // Step 2: Severity → base action
        Severity severity       = found.getSeverity();
        ModerationAction baseAction = severityEngine.evaluate(severity);

        // Step 3: Context override
        ModerationAction contextAction = contextManager.resolveAction(context, found, baseAction);

        // Step 4: Strike escalation (only apply if action is not already BLOCK/PERMANENT_BLOCK)
        ModerationAction finalAction;
        String reason;

        if (contextAction == ModerationAction.ALLOW_WITH_WARNING
                && severity != Severity.LOW) {
            // Context downgraded severity — skip strike escalation
            finalAction = contextAction;
            reason = String.format("Context '%s' allows word '%s' with warning.", context, found.getWord());
        } else if (contextAction == ModerationAction.BLOCK
                || contextAction == ModerationAction.PERMANENT_BLOCK) {
            // Already maximum action
            strikeManager.addStrikeAndGetAction(userId);
            finalAction = contextAction;
            reason = String.format("Word '%s' is blocked in context '%s'.", found.getWord(), context);
        } else {
            // Normal escalation through strikes
            ModerationAction strikeAction = strikeManager.addStrikeAndGetAction(userId);
            // Take the more severe of the two
            finalAction = moreServerAction(contextAction, strikeAction);
            reason = String.format("Word '%s' [%s] | Strikes: %d | Strike action: %s",
                    found.getWord(), severity, strikeManager.getStrikeCount(userId), strikeAction);
        }

        // Step 5: Log
        auditLogger.log(userId, found.getWord(), finalAction, severity, reason);

        return new ModerationResult(text, normalized, found.getWord(), severity, finalAction, reason);
    }

    /**
     * Returns the more restrictive of two actions.
     */
    private ModerationAction moreServerAction(ModerationAction a, ModerationAction b) {
        int rankA = rank(a);
        int rankB = rank(b);
        return rankA >= rankB ? a : b;
    }

    private int rank(ModerationAction action) {
        switch (action) {
            case ALLOW:              return 0;
            case ALLOW_WITH_WARNING: return 1;
            case FLAG:               return 2;
            case TEMPORARY_BLOCK:    return 3;
            case BLOCK:              return 4;
            case PERMANENT_BLOCK:    return 5;
            default:                 return 0;
        }
    }

    // --- Accessors for sub-components (used in tests) ---
    public WordRepository    getWordRepository()    { return wordRepo; }
    public StrikeManager     getStrikeManager()     { return strikeManager; }
    public AuditLogger       getAuditLogger()       { return auditLogger; }
    public SeverityEngine    getSeverityEngine()    { return severityEngine; }
    public ContextRuleManager getContextManager()   { return contextManager; }
}
