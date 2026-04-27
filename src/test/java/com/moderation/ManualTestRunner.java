package com.moderation;

import com.moderation.engine.*;
import com.moderation.log.AuditLogger;
import com.moderation.model.*;
import com.moderation.util.*;
import java.util.*;

/**
 * Manual Test Runner - runs all test scenarios without JUnit dependency.
 * Simulates exactly what JUnit would test.
 * Output format: PASS / FAIL per test, with summary at end.
 */
public class ManualTestRunner {

    static int passed = 0;
    static int failed = 0;
    static List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println(" CONTENT MODERATION ENGINE - MANUAL TEST RUNNER");
        System.out.println(" (Simulates JUnit 5 test output - same assertions)");
        System.out.println("=================================================================\n");

        runSeverityEngineTests();
        runTextMatchingTests();
        runStrikeContextTests();
        runAuditLoggerTests();
        runIntegrationTests();

        printFinalSummary();
    }

    // ====================================================================
    // PERSON 1 - SeverityEngine Tests
    // ====================================================================
    static void runSeverityEngineTests() {
        section("PERSON 1 - SeverityEngineTest");
        SeverityEngine e = new SeverityEngine();

        test("HIGH severity -> BLOCK",
            e.evaluate(Severity.HIGH) == ModerationAction.BLOCK);
        test("MEDIUM severity -> FLAG",
            e.evaluate(Severity.MEDIUM) == ModerationAction.FLAG);
        test("LOW severity -> ALLOW_WITH_WARNING",
            e.evaluate(Severity.LOW) == ModerationAction.ALLOW_WITH_WARNING);
        test("null severity -> ALLOW",
            e.evaluate(null) == ModerationAction.ALLOW);
        test("describeAction(HIGH) mentions 'block'",
            e.describeAction(Severity.HIGH).toLowerCase().contains("block"));
        test("describeAction(MEDIUM) mentions 'flag'",
            e.describeAction(Severity.MEDIUM).toLowerCase().contains("flag"));
        test("describeAction(LOW) mentions 'warning'",
            e.describeAction(Severity.LOW).toLowerCase().contains("warning"));
        test("describeAction(null) non-null",
            e.describeAction(null) != null);
        test("HIGH does NOT return ALLOW",
            e.evaluate(Severity.HIGH) != ModerationAction.ALLOW);
        test("LOW does NOT return BLOCK",
            e.evaluate(Severity.LOW) != ModerationAction.BLOCK);
    }

    // ====================================================================
    // PERSON 2 - TextNormalizer & TextMatcher Tests
    // ====================================================================
    static void runTextMatchingTests() {
        section("PERSON 2 - TextMatchingTest");

        test("normalize() lowercases: KILL -> kill",
            TextNormalizer.normalize("KILL").equals("kill"));
        test("normalize() mixed case: BoMb -> bomb",
            TextNormalizer.normalize("BoMb").equals("bomb"));
        test("normalize() leet 1->i: k1ll -> kill",
            TextNormalizer.normalize("k1ll").equals("kill"));
        test("normalize() leet 0->o: b0mb -> bomb",
            TextNormalizer.normalize("b0mb").equals("bomb"));
        test("normalize() leet @->a: b@d -> bad",
            TextNormalizer.normalize("b@d").equals("bad"));
        test("normalize() $->s: $cam -> scam",
            TextNormalizer.normalize("$cam").equals("scam"));
        test("normalize() strips hyphens: bad-word has no hyphen",
            !TextNormalizer.normalize("bad-word").contains("-"));
        test("normalizeSimple() keeps digits: B0MB! -> b0mb",
            TextNormalizer.normalizeSimple("B0MB!").equals("b0mb"));
        test("normalize(null) -> empty string",
            TextNormalizer.normalize(null).equals(""));

        List<BannedWord> dict = Arrays.asList(
            new BannedWord("kill",  Severity.HIGH),
            new BannedWord("bomb",  Severity.HIGH),
            new BannedWord("drug",  Severity.MEDIUM),
            new BannedWord("idiot", Severity.LOW),
            new BannedWord("scam",  Severity.MEDIUM)
        );

        test("TextMatcher detects UPPERCASE KILL",
            TextMatcher.findMatch("KILL them", dict) != null);
        test("TextMatcher: clean text -> null",
            TextMatcher.findMatch("Hello world!", dict) == null);
        test("TextMatcher detects k1ll (leet)",
            TextMatcher.findMatch("I will k1ll you", dict) != null &&
            TextMatcher.findMatch("I will k1ll you", dict).getWord().equals("kill"));
        test("TextMatcher detects b0mb (leet)",
            TextMatcher.findMatch("plant a b0mb", dict) != null &&
            TextMatcher.findMatch("plant a b0mb", dict).getWord().equals("bomb"));
        test("TextMatcher detects $cam (symbol)",
            TextMatcher.findMatch("This is a $cam!", dict) != null);
        test("TextMatcher detects partial: killzone triggers kill",
            TextMatcher.findMatch("killzone is a game", dict) != null);
        test("containsWord true for leet word",
            TextMatcher.containsWord("I will k1ll you", "kill"));
        test("containsWord false for absent word",
            !TextMatcher.containsWord("Hello world", "bomb"));
        test("containsWord null text -> false",
            !TextMatcher.containsWord(null, "kill"));
    }

    // ====================================================================
    // PERSON 3 - StrikeManager & ContextRuleManager Tests
    // ====================================================================
    static void runStrikeContextTests() {
        section("PERSON 3 - StrikeContextTest");
        StrikeManager sm = new StrikeManager();

        test("New user starts with 0 strikes",
            sm.getStrikeCount("newUser") == 0);
        sm.addStrikeAndGetAction("user1");
        test("Strike count increments to 1 after first offence",
            sm.getStrikeCount("user1") == 1);

        sm.addStrikeAndGetAction("user2");
        sm.addStrikeAndGetAction("user2");
        sm.addStrikeAndGetAction("user2");
        test("Three calls produce strike count of 3",
            sm.getStrikeCount("user2") == 3);

        StrikeManager sm2 = new StrikeManager();
        test("1st strike -> ALLOW_WITH_WARNING",
            sm2.addStrikeAndGetAction("userA") == ModerationAction.ALLOW_WITH_WARNING);
        test("2nd strike -> TEMPORARY_BLOCK",
            sm2.addStrikeAndGetAction("userA") == ModerationAction.TEMPORARY_BLOCK);
        test("3rd strike -> PERMANENT_BLOCK",
            sm2.addStrikeAndGetAction("userA") == ModerationAction.PERMANENT_BLOCK);
        test("4th strike still PERMANENT_BLOCK",
            sm2.addStrikeAndGetAction("userA") == ModerationAction.PERMANENT_BLOCK);

        StrikeManager sm3 = new StrikeManager();
        sm3.addStrikeAndGetAction("userD");
        sm3.addStrikeAndGetAction("userD");
        sm3.resetStrikes("userD");
        test("resetStrikes clears count to 0",
            sm3.getStrikeCount("userD") == 0);

        StrikeManager sm4 = new StrikeManager();
        sm4.addStrikeAndGetAction("userE");
        sm4.addStrikeAndGetAction("userE");
        sm4.addStrikeAndGetAction("userF");
        test("Different users have independent strike counts (E=2)",
            sm4.getStrikeCount("userE") == 2);
        test("Different users have independent strike counts (F=1)",
            sm4.getStrikeCount("userF") == 1);

        StrikeManager sm5 = new StrikeManager();
        sm5.peekNextAction("peekUser");
        test("peekNextAction does NOT increment strike count",
            sm5.getStrikeCount("peekUser") == 0);

        ContextRuleManager cm = new ContextRuleManager();
        BannedWord violence = new BannedWord("violence", Severity.MEDIUM);
        BannedWord kill     = new BannedWord("kill",     Severity.HIGH);
        BannedWord drug     = new BannedWord("drug",     Severity.MEDIUM);
        BannedWord cheat    = new BannedWord("cheat",    Severity.MEDIUM);

        test("EDUCATIONAL allows violence -> ALLOW_WITH_WARNING",
            cm.getOverride(Context.EDUCATIONAL, violence, ModerationAction.FLAG)
              == ModerationAction.ALLOW_WITH_WARNING);
        test("EDUCATIONAL allows kill -> ALLOW_WITH_WARNING",
            cm.getOverride(Context.EDUCATIONAL, kill, ModerationAction.BLOCK)
              == ModerationAction.ALLOW_WITH_WARNING);
        test("GENERAL has no override (null)",
            cm.getOverride(Context.GENERAL, kill, ModerationAction.BLOCK) == null);
        test("MEDICAL allows drug -> ALLOW_WITH_WARNING",
            cm.getOverride(Context.MEDICAL, drug, ModerationAction.FLAG)
              == ModerationAction.ALLOW_WITH_WARNING);
        test("GAMING blocks cheat -> BLOCK",
            cm.getOverride(Context.GAMING, cheat, ModerationAction.FLAG)
              == ModerationAction.BLOCK);
        test("resolveAction uses override when present (EDU+kill -> ALLOW_WITH_WARNING)",
            cm.resolveAction(Context.EDUCATIONAL, kill, ModerationAction.BLOCK)
              == ModerationAction.ALLOW_WITH_WARNING);
        test("resolveAction uses default when no override (GENERAL+kill -> BLOCK)",
            cm.resolveAction(Context.GENERAL, kill, ModerationAction.BLOCK)
              == ModerationAction.BLOCK);
        test("null context -> null override",
            cm.getOverride(null, kill, ModerationAction.BLOCK) == null);
    }

    // ====================================================================
    // PERSON 4 - AuditLogger Tests
    // ====================================================================
    static void runAuditLoggerTests() {
        section("PERSON 4 - AuditLoggerTest");
        AuditLogger logger = new AuditLogger();

        test("Fresh logger has 0 entries",
            logger.getTotalLogCount() == 0);
        test("isEmpty() true on fresh logger",
            logger.isEmpty());

        logger.log("user1", "kill", ModerationAction.BLOCK, Severity.HIGH, "Test");
        test("One log() call produces one entry",
            logger.getTotalLogCount() == 1);
        test("isEmpty() false after logging",
            !logger.isEmpty());

        logger.log("user2", "idiot",  ModerationAction.ALLOW_WITH_WARNING, Severity.LOW,    "r2");
        logger.log("user3", "weapon", ModerationAction.FLAG,               Severity.MEDIUM, "r3");
        test("Three log calls produce exactly 3 entries",
            logger.getTotalLogCount() == 3);

        AuditLog log0 = logger.getAllLogs().get(0);
        test("Log records correct userId: alice",
            log0.getUserId().equals("user1"));
        test("Log records correct triggeredWord: kill",
            log0.getTriggeredWord().equals("kill"));
        test("Log records correct action: BLOCK",
            log0.getAction() == ModerationAction.BLOCK);
        test("Log records correct severity: HIGH",
            log0.getSeverity() == Severity.HIGH);
        test("Timestamp is non-null",
            log0.getTimestamp() != null);

        AuditLogger l2 = new AuditLogger();
        l2.log("u1", "kill", ModerationAction.BLOCK, Severity.HIGH, "r");
        l2.log("u2", "idiot",ModerationAction.ALLOW_WITH_WARNING, Severity.LOW, "r");
        l2.log("u1", "bomb", ModerationAction.BLOCK, Severity.HIGH, "r");
        test("getLogsForUser returns 2 entries for user1",
            l2.getLogsForUser("u1").size() == 2);
        test("getLogsForUser returns empty for unknown user",
            l2.getLogsForUser("nobody").isEmpty());
        test("getLogsByAction(BLOCK) returns 2",
            l2.getLogsByAction(ModerationAction.BLOCK).size() == 2);

        AuditLogger l3 = new AuditLogger();
        l3.log("u1","kill",  ModerationAction.BLOCK,             Severity.HIGH,   "r");
        l3.log("u2","drug",  ModerationAction.FLAG,              Severity.MEDIUM, "r");
        l3.log("u3","idiot", ModerationAction.ALLOW_WITH_WARNING,Severity.LOW,    "r");
        l3.log("u4","bomb",  ModerationAction.BLOCK,             Severity.HIGH,   "r");
        test("getLogsBySeverity(HIGH) returns 2",
            l3.getLogsBySeverity(Severity.HIGH).size() == 2);

        AuditLogger l4 = new AuditLogger();
        l4.log("u","kill",ModerationAction.BLOCK,Severity.HIGH,"r");
        l4.clearLogs();
        test("clearLogs resets count to 0",
            l4.getTotalLogCount() == 0);

        AuditLogger l5 = new AuditLogger();
        l5.log("a","kill", ModerationAction.BLOCK,             Severity.HIGH,"first");
        l5.log("b","idiot",ModerationAction.ALLOW_WITH_WARNING,Severity.LOW, "second");
        List<AuditLog> all = l5.getAllLogs();
        test("getAllLogs returns entries in insertion order",
            all.get(0).getUserId().equals("a") && all.get(1).getUserId().equals("b"));
    }

    // ====================================================================
    // PERSON 4 - Integration Tests (Full Pipeline)
    // ====================================================================
    static void runIntegrationTests() {
        section("PERSON 4 - ModerationEngineIntegrationTest (Full Pipeline)");

        ModerationEngine engine;

        engine = new ModerationEngine();
        test("Clean content -> ALLOW",
            engine.moderate("u1","Hello have a great day!", Context.GENERAL).getAction()
              == ModerationAction.ALLOW);

        engine = new ModerationEngine();
        engine.moderate("u1","Good morning!", Context.GENERAL);
        test("Clean content still produces audit log entry",
            engine.getAuditLogger().getTotalLogCount() == 1);

        engine = new ModerationEngine();
        test("isClean() true for clean content",
            engine.moderate("u0","Nice weather!", Context.GENERAL).isClean());

        engine = new ModerationEngine();
        test("HIGH severity word -> BLOCK",
            engine.moderate("u2","I will kill you!", Context.GENERAL).getAction()
              == ModerationAction.BLOCK);

        engine = new ModerationEngine();
        test("MEDIUM severity word is NOT ALLOW",
            engine.moderate("uX","I enjoy violence", Context.GENERAL).getAction()
              != ModerationAction.ALLOW);

        engine = new ModerationEngine();
        test("LOW severity 1st strike -> ALLOW_WITH_WARNING",
            engine.moderate("u3","You are stupid", Context.GENERAL).getAction()
              == ModerationAction.ALLOW_WITH_WARNING);

        engine = new ModerationEngine();
        ModerationResult leet = engine.moderate("u4","I will k1ll you", Context.GENERAL);
        test("Leet-speak 'k1ll' is detected",
            leet.getAction() != ModerationAction.ALLOW);
        test("Leet triggered word normalised to 'kill'",
            "kill".equals(leet.getTriggeredWord()));

        engine = new ModerationEngine();
        ModerationResult bomb = engine.moderate("u5","b0mb threat", Context.GENERAL);
        test("Symbol evasion 'b0mb' is detected",
            bomb.getTriggeredWord() != null);
        test("b0mb triggered word normalised to 'bomb'",
            "bomb".equals(bomb.getTriggeredWord()));

        engine = new ModerationEngine();
        ModerationResult nr = engine.moderate("u1","B0MB attack!", Context.GENERAL);
        test("Result contains normalised text containing 'bomb'",
            nr.getNormalizedText() != null && nr.getNormalizedText().contains("bomb"));

        // Strike escalation with repeated LOW word
        engine = new ModerationEngine();
        ModerationResult r1 = engine.moderate("repeat_user","You are an idiot",Context.GENERAL);
        ModerationResult r2 = engine.moderate("repeat_user","What a loser",    Context.GENERAL);
        ModerationResult r3 = engine.moderate("repeat_user","Stop being dumb", Context.GENERAL);
        test("Strike 1 -> ALLOW_WITH_WARNING",
            r1.getAction() == ModerationAction.ALLOW_WITH_WARNING);
        test("Strike 2 -> TEMPORARY_BLOCK",
            r2.getAction() == ModerationAction.TEMPORARY_BLOCK);
        test("Strike 3 -> PERMANENT_BLOCK",
            r3.getAction() == ModerationAction.PERMANENT_BLOCK);

        engine = new ModerationEngine();
        test("EDUCATIONAL context: 'kill' -> ALLOW_WITH_WARNING",
            engine.moderate("student1","The causes of kill in war",Context.EDUCATIONAL).getAction()
              == ModerationAction.ALLOW_WITH_WARNING);

        engine = new ModerationEngine();
        test("MEDICAL context: 'drug' -> ALLOW_WITH_WARNING",
            engine.moderate("doc1","The patient was prescribed a drug",Context.MEDICAL).getAction()
              == ModerationAction.ALLOW_WITH_WARNING);

        engine = new ModerationEngine();
        test("GAMING context: 'cheat' -> BLOCK",
            engine.moderate("gamer1","I know how to cheat",Context.GAMING).getAction()
              == ModerationAction.BLOCK);

        engine = new ModerationEngine();
        engine.moderate("audit_user","You are stupid",Context.GENERAL);
        test("Audit log records correct userId",
            engine.getAuditLogger().getAllLogs().get(0).getUserId().equals("audit_user"));

        engine = new ModerationEngine();
        engine.moderate("u1","Hello world",   Context.GENERAL);
        engine.moderate("u2","I will kill",   Context.GENERAL);
        engine.moderate("u3","You are stupid",Context.GENERAL);
        test("Audit log count matches number of moderate() calls",
            engine.getAuditLogger().getTotalLogCount() == 3);

        engine = new ModerationEngine();
        engine.moderate("u1","Don't be an idiot",Context.GENERAL);
        test("Audit log records triggered word: idiot",
            "idiot".equals(engine.getAuditLogger().getAllLogs().get(0).getTriggeredWord()));

        engine = new ModerationEngine();
        ModerationResult res = engine.moderate("u1","I will bomb you",Context.GENERAL);
        test("Result has non-null non-empty reason",
            res.getReason() != null && !res.getReason().isEmpty());

        engine = new ModerationEngine();
        test("Clean result has null triggeredWord",
            engine.moderate("u1","Nice day",Context.GENERAL).getTriggeredWord() == null);
    }

    // ====================================================================
    // Helpers
    // ====================================================================
    static void test(String name, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s%n", name);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s%n", name);
            failed++;
            failures.add(name);
        }
    }

    static void section(String title) {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println(" " + title);
        System.out.println("----------------------------------------------------------------");
    }

    static void printFinalSummary() {
        int total = passed + failed;
        System.out.println("\n=================================================================");
        System.out.println(" TEST RESULTS SUMMARY");
        System.out.println("=================================================================");
        System.out.printf(" Tests run    : %d%n", total);
        System.out.printf(" Tests passed : %d%n", passed);
        System.out.printf(" Tests failed : %d%n", failed);
        System.out.println("-----------------------------------------------------------------");
        if (failed == 0) {
            System.out.println(" STATUS: BUILD SUCCESS - ALL TESTS PASSED");
        } else {
            System.out.println(" STATUS: BUILD FAILURE - SOME TESTS FAILED");
            System.out.println("\n Failed tests:");
            failures.forEach(f -> System.out.println("   FAILED: " + f));
        }
        System.out.println("=================================================================");
    }
}
