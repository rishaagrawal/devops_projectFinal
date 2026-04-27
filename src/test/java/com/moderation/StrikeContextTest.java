package com.moderation;

import com.moderation.engine.ContextRuleManager;
import com.moderation.engine.StrikeManager;
import com.moderation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TEST MODULE : Strike Manager & Context Rule Manager Tests
 * PERSON      : Person 3 - Strike & Context Rule Manager
 * BRANCH      : feature-strikes-context
 * ============================================================
 *
 * Verifies:
 *  - Strike count increments correctly
 *  - Correct action is returned at each strike level
 *  - Context overrides (whitelist / blocklist) work as expected
 *  - resolveAction returns override when present, default otherwise
 *
 * Run with Maven: mvn test -pl . -Dtest=StrikeContextTest
 */
@DisplayName("Person 3 - Strike Manager & Context Rule Manager Tests")
public class StrikeContextTest {

    private StrikeManager      strikeManager;
    private ContextRuleManager contextManager;

    @BeforeEach
    void setUp() {
        strikeManager  = new StrikeManager();
        contextManager = new ContextRuleManager();
    }

    // ????????????????????????????????????????????????????????
    //  STRIKE MANAGER TESTS
    // ????????????????????????????????????????????????????????

    @Test
    @DisplayName("New user starts with 0 strikes")
    void testInitialStrikeCountIsZero() {
        assertEquals(0, strikeManager.getStrikeCount("newUser"),
                "A brand-new user should have zero strikes");
    }

    @Test
    @DisplayName("Strike count increments to 1 after first offence")
    void testFirstStrikeIncrementsCount() {
        strikeManager.addStrikeAndGetAction("user1");
        assertEquals(1, strikeManager.getStrikeCount("user1"),
                "Strike count should be 1 after one violation");
    }

    @Test
    @DisplayName("Three calls produce strike count of 3")
    void testThreeStrikesIncrementCorrectly() {
        strikeManager.addStrikeAndGetAction("user2");
        strikeManager.addStrikeAndGetAction("user2");
        strikeManager.addStrikeAndGetAction("user2");
        assertEquals(3, strikeManager.getStrikeCount("user2"),
                "Strike count should be 3 after three violations");
    }

    // ?? Action per strike level ???????????????????????????????????????

    @Test
    @DisplayName("1st strike -> ALLOW_WITH_WARNING")
    void testFirstStrikeReturnsWarning() {
        ModerationAction action = strikeManager.addStrikeAndGetAction("userA");
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, action,
                "1st strike must return ALLOW_WITH_WARNING");
    }

    @Test
    @DisplayName("2nd strike -> TEMPORARY_BLOCK")
    void testSecondStrikeReturnsTemporaryBlock() {
        strikeManager.addStrikeAndGetAction("userB");
        ModerationAction action = strikeManager.addStrikeAndGetAction("userB");
        assertEquals(ModerationAction.TEMPORARY_BLOCK, action,
                "2nd strike must return TEMPORARY_BLOCK");
    }

    @Test
    @DisplayName("3rd strike -> PERMANENT_BLOCK")
    void testThirdStrikeReturnsPermanentBlock() {
        strikeManager.addStrikeAndGetAction("userC");
        strikeManager.addStrikeAndGetAction("userC");
        ModerationAction action = strikeManager.addStrikeAndGetAction("userC");
        assertEquals(ModerationAction.PERMANENT_BLOCK, action,
                "3rd strike must return PERMANENT_BLOCK");
    }

    @Test
    @DisplayName("4th+ strike also returns PERMANENT_BLOCK")
    void testFourthStrikeStillPermanentBlock() {
        for (int i = 0; i < 4; i++) strikeManager.addStrikeAndGetAction("userX");
        assertEquals(ModerationAction.PERMANENT_BLOCK,
                strikeManager.addStrikeAndGetAction("userX"),
                "Any strike beyond 3 should still return PERMANENT_BLOCK");
    }

    // ?? Reset / isolation ????????????????????????????????????????????

    @Test
    @DisplayName("resetStrikes clears the count to 0")
    void testResetStrikesClearsCount() {
        strikeManager.addStrikeAndGetAction("userD");
        strikeManager.addStrikeAndGetAction("userD");
        strikeManager.resetStrikes("userD");
        assertEquals(0, strikeManager.getStrikeCount("userD"),
                "Strike count should be 0 after reset");
    }

    @Test
    @DisplayName("Different users have independent strike counts")
    void testDifferentUsersHaveIndependentStrikes() {
        strikeManager.addStrikeAndGetAction("userE");
        strikeManager.addStrikeAndGetAction("userE");
        strikeManager.addStrikeAndGetAction("userF");
        assertEquals(2, strikeManager.getStrikeCount("userE"),
                "userE should have 2 strikes");
        assertEquals(1, strikeManager.getStrikeCount("userF"),
                "userF should have 1 strike");
    }

    @Test
    @DisplayName("peekNextAction does not increment the counter")
    void testPeekNextActionDoesNotIncrement() {
        strikeManager.peekNextAction("peekUser");
        assertEquals(0, strikeManager.getStrikeCount("peekUser"),
                "peekNextAction must not change the strike count");
    }

    // ????????????????????????????????????????????????????????
    //  CONTEXT RULE MANAGER TESTS
    // ????????????????????????????????????????????????????????

    @Test
    @DisplayName("EDUCATIONAL context allows 'violence' -> ALLOW_WITH_WARNING")
    void testEducationalContextAllowsViolenceWord() {
        BannedWord violence = new BannedWord("violence", Severity.MEDIUM);
        ModerationAction override = contextManager.getOverride(
                Context.EDUCATIONAL, violence, ModerationAction.FLAG);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, override,
                "EDUCATIONAL context should downgrade 'violence' to ALLOW_WITH_WARNING");
    }

    @Test
    @DisplayName("EDUCATIONAL context allows 'kill' -> ALLOW_WITH_WARNING")
    void testEducationalContextAllowsKillWord() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction override = contextManager.getOverride(
                Context.EDUCATIONAL, kill, ModerationAction.BLOCK);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, override,
                "EDUCATIONAL context should downgrade 'kill' to ALLOW_WITH_WARNING");
    }

    @Test
    @DisplayName("GENERAL context has no override (returns null)")
    void testGeneralContextNoOverride() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction override = contextManager.getOverride(
                Context.GENERAL, kill, ModerationAction.BLOCK);
        assertNull(override,
                "GENERAL context should return null (no override)");
    }

    @Test
    @DisplayName("MEDICAL context allows 'drug' -> ALLOW_WITH_WARNING")
    void testMedicalContextAllowsDrugWord() {
        BannedWord drug = new BannedWord("drug", Severity.MEDIUM);
        ModerationAction override = contextManager.getOverride(
                Context.MEDICAL, drug, ModerationAction.FLAG);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, override,
                "MEDICAL context should allow 'drug' with warning");
    }

    @Test
    @DisplayName("GAMING context blocks 'cheat' -> BLOCK")
    void testGamingContextBlocksCheatWord() {
        BannedWord cheat = new BannedWord("cheat", Severity.MEDIUM);
        ModerationAction override = contextManager.getOverride(
                Context.GAMING, cheat, ModerationAction.FLAG);
        assertEquals(ModerationAction.BLOCK, override,
                "GAMING context should escalate 'cheat' to BLOCK");
    }

    @Test
    @DisplayName("resolveAction returns override when one exists")
    void testResolveActionUsesOverrideWhenPresent() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction resolved = contextManager.resolveAction(
                Context.EDUCATIONAL, kill, ModerationAction.BLOCK);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, resolved,
                "resolveAction should apply override in EDUCATIONAL context");
    }

    @Test
    @DisplayName("resolveAction returns default when no override exists")
    void testResolveActionUsesDefaultWhenNoOverride() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        ModerationAction resolved = contextManager.resolveAction(
                Context.GENERAL, kill, ModerationAction.BLOCK);
        assertEquals(ModerationAction.BLOCK, resolved,
                "resolveAction should return default when no override");
    }

    @Test
    @DisplayName("getOverride with null context returns null")
    void testNullContextReturnsNull() {
        BannedWord kill = new BannedWord("kill", Severity.HIGH);
        assertNull(contextManager.getOverride(null, kill, ModerationAction.BLOCK),
                "null context should produce no override");
    }
}
