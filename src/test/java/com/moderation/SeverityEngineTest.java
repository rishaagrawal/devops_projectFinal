package com.moderation;

import com.moderation.engine.SeverityEngine;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SeverityEngineTest {

    private SeverityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SeverityEngine();
    }

    @Test
    void testHighSeverityReturnsBlock() {
        ModerationAction action = engine.evaluate(Severity.HIGH);
        assertEquals(ModerationAction.BLOCK, action,
                "HIGH severity should result in BLOCK");
    }

    @Test
    void testMediumSeverityReturnsFlag() {
        ModerationAction action = engine.evaluate(Severity.MEDIUM);
        assertEquals(ModerationAction.FLAG, action,
                "MEDIUM severity should result in FLAG");
    }

    @Test
    void testLowSeverityReturnsAllowWithWarning() {
        ModerationAction action = engine.evaluate(Severity.LOW);
        assertEquals(ModerationAction.ALLOW_WITH_WARNING, action,
                "LOW severity should result in ALLOW_WITH_WARNING");
    }

    @Test
    void testNullSeverityReturnsAllow() {
        ModerationAction action = engine.evaluate(null);
        assertEquals(ModerationAction.ALLOW, action,
                "Null severity should result in ALLOW");
    }

    @Test
    void testDescribeActionForHigh() {
        String desc = engine.describeAction(Severity.HIGH);
        assertTrue(desc.toLowerCase().contains("block"),
                "Description for HIGH should mention block");
    }

    @Test
    void testDescribeActionForMedium() {
        String desc = engine.describeAction(Severity.MEDIUM);
        assertTrue(desc.toLowerCase().contains("flag"),
                "Description for MEDIUM should mention flag");
    }

    @Test
    void testDescribeActionForLow() {
        String desc = engine.describeAction(Severity.LOW);
        assertTrue(desc.toLowerCase().contains("warning"),
                "Description for LOW should mention warning");
    }
}
