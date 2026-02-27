package com.moderation;

import com.moderation.engine.ContextRuleManager;
import com.moderation.engine.StrikeManager;
import com.moderation.model.BannedWord;
import com.moderation.model.Context;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StrikeContextTest {

    private StrikeManager     strikeManager;
    private ContextRuleManager contextManager;

    @BeforeEach
    void setUp() {
        strikeManager   = new StrikeManager();
        contextManager  = new ContextRuleManager();
    }

    // ---- Strike Count Tests ----

    @Test
    void testInitialStrikeCountIsZero() {
        assertEquals(0, strikeManager.getStrikeCount("newUser"),
                "A new user should have 0 strikes");
    }

    @Test
    void testFirstStrikeIncrementsCount() {
        strikeManager.addStrikeAndGetAction("user1");
        assertEquals(1, strikeManager.getStrikeCount("user1"),
                "Strike count should be 1 after first offence");
    }

    @Test
    void testThreeStrikesIncrementCorrectly() {
        strikeManager.addStrikeAndGetAction("user2");
        strikeManager.addStrikeAndGetAction("user2");
        strikeManager.addStrikeAndGetAction("user2");
        assertEquals(3, strikeManager.getStrikeCount("user2"),
                "Strike count should be 3 after three offences");
    }

    // ---- Action per Strike Level Tests ----

    @Test
    void testFirstStrikeReturnsWarning() {
        ModerationAction action = strikeManager.addStrikeAndGetAction("userA");
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, action,
                "1st strike should return ALLOW_WITH_WARNING");
    }

    @Test
    void testSecondStrikeReturnsTemporaryBlock() {
        strikeManager.addStrikeAndGetAction("userB");
        ModerationAction action = strikeManager.addStrikeAndGetAction("userB");
        assertEquals(ModerationAction.TEMPORARY_BLOCK, action,
                "2nd strike should return TEMPORARY_BLOCK");
    }

    @Test
    void testThirdStrikeReturnsPermanentBlock() {
        strikeManager.addStrikeAndGetAction("userC");
        strikeManager.addStrikeAndGetAction("userC");
        ModerationAction action = strikeManager.addStrikeAndGetAction("userC");
        assertEquals(ModerationAction.PERMANENT_BLOCK, action,
                "3rd strike should return PERMANENT_BLOCK");
    }

    @Test
    void testResetStrikesClearsCount() {
        strikeManager.addStrikeAndGetAction("userD");
        strikeManager.addStrikeAndGetAction("userD");
        strikeManager.resetStrikes("userD");
        assertEquals(0, strikeManager.getStrikeCount("userD"),
                "Strike count should be 0 after reset");
    }

    @Test
    void testDifferentUsersHaveIndependentStrikes() {
        strikeManager.addStrikeAndGetAction("userE");
        strikeManager.addStrikeAndGetAction("userE");
        strikeManager.addStrikeAndGetAction("userF"); // only 1 strike
        assertEquals(2, strikeManager.getStrikeCount("userE"));
        assertEquals(1, strikeManager.getStrikeCount("userF"));
    }

    // ---- Context Rule Tests ----

    @Test
    void testEducationalContextAllowsViolenceWord() {
        BannedWord violence = new BannedWord("violence", Severity.MEDIUM);
        ModerationAction override = contextManager.getOverride(
                Context.EDUCATIONAL, violence, ModerationAction.FLAG);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, override,
                "EDUCATIONAL context should allow 'violence' with warning");
    }

    @Test
    void testEducationalContextAllowsKillWord() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction override = contextManager.getOverride(
                Context.EDUCATIONAL, kill, ModerationAction.BLOCK);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, override,
                "EDUCATIONAL context should allow 'kill' with warning");
    }

    @Test
    void testGeneralContextNoOverride() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction override = contextManager.getOverride(
                Context.GENERAL, kill, ModerationAction.BLOCK);
        assertNull(override,
                "GENERAL context should return null override (use default)");
    }

    @Test
    void testMedicalContextAllowsDrugWord() {
        BannedWord drug = new BannedWord("drug", Severity.MEDIUM);
        ModerationAction override = contextManager.getOverride(
                Context.MEDICAL, drug, ModerationAction.FLAG);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, override,
                "MEDICAL context should allow 'drug' with warning");
    }

    @Test
    void testGamingContextBlocksCheatWord() {
        BannedWord cheat = new BannedWord("cheat", Severity.MEDIUM);
        ModerationAction override = contextManager.getOverride(
                Context.GAMING, cheat, ModerationAction.FLAG);
        assertEquals(ModerationAction.BLOCK, override,
                "GAMING context should block 'cheat'");
    }

    @Test
    void testResolveActionUsesOverrideWhenPresent() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction resolved = contextManager.resolveAction(
                Context.EDUCATIONAL, kill, ModerationAction.BLOCK);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, resolved,
                "resolveAction should use context override when available");
    }

    @Test
    void testResolveActionUsesDefaultWhenNoOverride() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction resolved = contextManager.resolveAction(
                Context.GENERAL, kill, ModerationAction.BLOCK);
        assertEquals(ModerationAction.BLOCK, resolved,
                "resolveAction should use default when no override present");
    }
}
