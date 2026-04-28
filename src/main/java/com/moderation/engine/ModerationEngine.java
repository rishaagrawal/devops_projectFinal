package com.moderation.engine;

import com.moderation.log.AuditLogger;
import com.moderation.model.*;
import com.moderation.util.TextMatcher;
import com.moderation.util.TextNormalizer;

/**
 * ============================================================
 * MODULE : Moderation Engine (Central Orchestrator)
 * ============================================================
 *
 * Pipeline executed for every call to {@link #moderate}:
 *
 *  Step 1 - Text Normalization  
 *  Step 2 - Text Matching       
 *  Step 3 - Severity Evaluation 
 *  Step 4 - Context Override    
 *  Step 5 - Strike Escalation  
 *  Step 6 - Audit Logging     
 *  Step 7 - Return ModerationResult
 */
public class ModerationEngine {

    private final WordRepository     wordRepo;
    private final SeverityEngine     severityEngine;
    private final StrikeManager      strikeManager;
    private final ContextRuleManager contextManager;
    private final AuditLogger        auditLogger;

    /** Default constructor - creates fresh instances of all sub-components. */
    public ModerationEngine() {
        this.wordRepo       = new WordRepository();
        this.severityEngine = new SeverityEngine();
        this.strikeManager  = new StrikeManager();
        this.contextManager = new ContextRuleManager();
        this.auditLogger    = new AuditLogger();
    }

    /**
     * Constructor for dependency injection (used in unit / integration tests).
     */
    public ModerationEngine(WordRepository wordRepo,
                            SeverityEngine severityEngine,
                            StrikeManager strikeManager,
                            ContextRuleManager contextManager,
                            AuditLogger auditLogger) {
        this.wordRepo       = wordRepo;
        this.severityEngine = severityEngine;
        this.strikeManager  = strikeManager;
        this.contextManager = contextManager;
        this.auditLogger    = auditLogger;
    }

    // ------------------------------------------------------------------
    //  Core moderation method
    // ------------------------------------------------------------------

    /**
     * Runs the full moderation pipeline on a piece of user-submitted text.
     *
     * @param userId  The user submitting the content (tracked for strikes)
     * @param text    The raw message text
     * @param context The platform context (GENERAL / EDUCATIONAL / MEDICAL / GAMING)
     * @return A {@link ModerationResult} describing the final decision
     */
    public ModerationResult moderate(String userId, String text, Context context) {

        // ?? Step 1: Normalize the text ???????????????????????????????????
        String normalized = TextNormalizer.normalize(text);

        // ?? Step 2: Text matching ????????????????????????????????????????
        BannedWord found = TextMatcher.findMatch(text, wordRepo.getBannedWords());

        if (found == null) {
            // Clean content - log it and return immediately
            auditLogger.log(userId, "none", ModerationAction.ALLOW, null, "No violations found.");
            return new ModerationResult(text, normalized, null, null,
                    ModerationAction.ALLOW, "No violations found.");
        }

        // ?? Step 3: Severity -> base action ??????????????????????????????
        Severity         severity   = found.getSeverity();
        ModerationAction baseAction = severityEngine.evaluate(severity);

        // ?? Step 4: Context override ?????????????????????????????????????
        ModerationAction contextAction = contextManager.resolveAction(context, found, baseAction);

        // ?? Step 5: Strike escalation ????????????????????????????????????
        ModerationAction finalAction;
        String reason;

        if (contextAction == ModerationAction.ALLOW_WITH_WARNING && severity != Severity.LOW) {
            // Context whitelisted this word -- skip strike escalation to avoid unfair penalties
            finalAction = contextAction;
            reason = String.format(
                    "Context '%s' allows word '%s' with warning (severity normally %s).",
                    context, found.getWord(), severity);

        } else if (contextAction == ModerationAction.BLOCK
                || contextAction == ModerationAction.PERMANENT_BLOCK) {
            // Already the maximum available action; still record the strike
            strikeManager.addStrikeAndGetAction(userId);
            finalAction = contextAction;
            reason = String.format(
                    "Word '%s' is always blocked in context '%s'.",
                    found.getWord(), context);

        } else {
            // Normal path: apply strike escalation and take the stricter result
            ModerationAction strikeAction = strikeManager.addStrikeAndGetAction(userId);
            finalAction = moreRestrictive(contextAction, strikeAction);
            reason = String.format(
                    "Word '%s' [%s] - strikes: %d - strike action: %s",
                    found.getWord(), severity,
                    strikeManager.getStrikeCount(userId), strikeAction);
        }

        // ?? Step 6: Audit log ?????????????????????????????????????????????
        auditLogger.log(userId, found.getWord(), finalAction, severity, reason);

        // ?? Step 7: Return result ?????????????????????????????????????????
        return new ModerationResult(text, normalized, found.getWord(), severity, finalAction, reason);
    }

    // ------------------------------------------------------------------
    //  Helper
    // ------------------------------------------------------------------

    /** Returns whichever of the two actions is more restrictive. */
    private ModerationAction moreRestrictive(ModerationAction a, ModerationAction b) {
        return rank(a) >= rank(b) ? a : b;
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

    // ------------------------------------------------------------------
    //  Accessors (exposed for menu-driven UI and tests)
    // ------------------------------------------------------------------

    public WordRepository     getWordRepository()  { return wordRepo; }
    public StrikeManager      getStrikeManager()   { return strikeManager; }
    public AuditLogger        getAuditLogger()     { return auditLogger; }
    public SeverityEngine     getSeverityEngine()  { return severityEngine; }
    public ContextRuleManager getContextManager()  { return contextManager; }
}
