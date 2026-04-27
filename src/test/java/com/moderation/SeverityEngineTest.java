package com.moderation;

import com.moderation.engine.SeverityEngine;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TEST MODULE : Severity Engine Tests
 * PERSON      : Person 1 - Core Moderation & Severity Engine
 * BRANCH      : feature-severity-engine
 * ============================================================
 *
 * Tests that SeverityEngine maps each severity level to the
 * correct base ModerationAction, including edge cases.
 *
 * Run with Maven: mvn test -pl . -Dtest=SeverityEngineTest
 */
@DisplayName("Person 1 - Severity Engine Tests")
public class SeverityEngineTest {

    private SeverityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SeverityEngine();
    }

    // ?? Core action mapping ???????????????????????????????????????????

    @Test
    @DisplayName("HIGH severity -> BLOCK")
    void testHighSeverityReturnsBlock() {
        ModerationAction action = engine.evaluate(Severity.HIGH);
        assertEquals(ModerationAction.BLOCK, action,
                "HIGH severity word must result in BLOCK");
    }

    @Test
    @DisplayName("MEDIUM severity -> FLAG")
    void testMediumSeverityReturnsFlag() {
        ModerationAction action = engine.evaluate(Severity.MEDIUM);
        assertEquals(ModerationAction.FLAG, action,
                "MEDIUM severity word must result in FLAG");
    }

    @Test
    @DisplayName("LOW severity -> ALLOW_WITH_WARNING")
    void testLowSeverityReturnsAllowWithWarning() {
        ModerationAction action = engine.evaluate(Severity.LOW);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, action,
                "LOW severity word must result in ALLOW_WITH_WARNING");
    }

    @Test
    @DisplayName("null severity -> ALLOW (clean content)")
    void testNullSeverityReturnsAllow() {
        ModerationAction action = engine.evaluate(null);
        assertEquals(ModerationAction.ALLOW, action,
                "null severity (clean content) must result in ALLOW");
    }

    // ?? describeAction() ?????????????????????????????????????????????

    @Test
    @DisplayName("describeAction(HIGH) mentions 'block'")
    void testDescribeActionForHigh() {
        String desc = engine.describeAction(Severity.HIGH);
        assertTrue(desc.toLowerCase().contains("block"),
                "Description for HIGH should mention 'block'");
    }

    @Test
    @DisplayName("describeAction(MEDIUM) mentions 'flag'")
    void testDescribeActionForMedium() {
        String desc = engine.describeAction(Severity.MEDIUM);
        assertTrue(desc.toLowerCase().contains("flag"),
                "Description for MEDIUM should mention 'flag'");
    }

    @Test
    @DisplayName("describeAction(LOW) mentions 'warning'")
    void testDescribeActionForLow() {
        String desc = engine.describeAction(Severity.LOW);
        assertTrue(desc.toLowerCase().contains("warning"),
                "Description for LOW should mention 'warning'");
    }

    @Test
    @DisplayName("describeAction(null) returns non-null string")
    void testDescribeActionForNull() {
        String desc = engine.describeAction(null);
        assertNotNull(desc, "describeAction with null severity should not return null");
    }

    // ?? Non-equality checks (avoid swapped cases) ?????????????????????

    @Test
    @DisplayName("HIGH does NOT return ALLOW or FLAG")
    void testHighIsNotAllowOrFlag() {
        ModerationAction action = engine.evaluate(Severity.HIGH);
        assertNotEquals(ModerationAction.ALLOW, action);
        assertNotEquals(ModerationAction.FLAG, action);
    }

    @Test
    @DisplayName("LOW does NOT return BLOCK or FLAG")
    void testLowIsNotBlockOrFlag() {
        ModerationAction action = engine.evaluate(Severity.LOW);
        assertNotEquals(ModerationAction.BLOCK, action);
        assertNotEquals(ModerationAction.FLAG, action);
    }
}