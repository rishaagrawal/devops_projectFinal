# Content Moderation Engine

A Java mini-project implementing a full content moderation pipeline with severity classification, text normalization, strike tracking, context-based rules, and audit logging.

---

## Project Structure

```
ContentModerationEngine/
├── pom.xml
└── src/
    ├── main/java/com/moderation/
    │   ├── Main.java                          ← Entry point / demo
    │   ├── model/
    │   │   ├── Severity.java                  ← LOW / MEDIUM / HIGH enum
    │   │   ├── ModerationAction.java          ← ALLOW / FLAG / BLOCK etc.
    │   │   ├── Context.java                   ← GENERAL / EDUCATIONAL / MEDICAL / GAMING
    │   │   ├── BannedWord.java                ← Word + severity pair
    │   │   ├── ModerationResult.java          ← Result returned by engine
    │   │   └── AuditLog.java                  ← Single log entry
    │   ├── util/
    │   │   ├── TextNormalizer.java            ← Lowercase, leet-speak, symbol stripping
    │   │   └── TextMatcher.java               ← Substring / partial matching
    │   └── engine/
    │       ├── SeverityEngine.java            ← Maps severity → action
    │       ├── WordRepository.java            ← Banned word dictionary
    │       ├── StrikeManager.java             ← Per-user strike tracking
    │       ├── ContextRuleManager.java        ← Context overrides
    │       ├── AuditLogger.java               ← Log history
    │       └── ModerationEngine.java          ← Orchestrates the full pipeline
    └── test/java/com/moderation/
        ├── SeverityEngineTest.java            ← Person 1 tests
        ├── TextMatchingTest.java              ← Person 2 tests
        ├── StrikeContextTest.java             ← Person 3 tests
        ├── AuditLoggerTest.java               ← Person 4 tests
        └── ModerationEngineIntegrationTest.java ← Full pipeline tests
```

---

## How It Works (Pipeline)

```
Input Text
    │
    ▼
TextNormalizer  ─── lowercase + leet-decode + strip symbols
    │
    ▼
TextMatcher     ─── find banned word in normalized text
    │
    ├── No match → ALLOW + log
    │
    ▼
SeverityEngine  ─── HIGH→BLOCK | MEDIUM→FLAG | LOW→ALLOW_WITH_WARNING
    │
    ▼
ContextRuleManager ─ EDUCATIONAL/MEDICAL can downgrade; GAMING can upgrade
    │
    ▼
StrikeManager   ─── 1st→warning | 2nd→temp block | 3rd→permanent block
    │
    ▼
AuditLogger     ─── records user, word, action, severity, timestamp
    │
    ▼
ModerationResult returned to caller
```

---

## Severity Rules

| Severity | Default Action     | Example Words                  |
|----------|--------------------|--------------------------------|
| HIGH     | BLOCK              | kill, bomb, hack, exploit      |
| MEDIUM   | FLAG               | drug, violence, weapon, scam   |
| LOW      | ALLOW_WITH_WARNING | idiot, stupid, dumb, loser     |

---

## Strike Escalation

| Strike | Action           |
|--------|------------------|
| 1st    | ALLOW_WITH_WARNING |
| 2nd    | TEMPORARY_BLOCK  |
| 3rd+   | PERMANENT_BLOCK  |

---

## Context Overrides

| Context     | Effect                                            |
|-------------|---------------------------------------------------|
| EDUCATIONAL | Whitelist: kill, violence, drug, suicide → WARNING |
| MEDICAL     | Whitelist: drug, overdose, death → WARNING        |
| GAMING      | Extra block: cheat, hack, exploit, glitch         |
| GENERAL     | No override — standard rules                      |

---

## Text Normalization Examples

| Input        | Normalized |
|--------------|------------|
| `KILL`       | `kill`     |
| `k1ll`       | `kill`     |
| `b@dword`    | `badword`  |
| `b0mb`       | `bomb`     |
| `bad-word`   | `badword`  |
| `$cam`       | `scam`     |

---

## Running the Project

### Prerequisites
- Java 11+
- Maven 3.6+

### Run Demo
```bash
mvn compile exec:java -Dexec.mainClass="com.moderation.Main"
```
Or:
```bash
mvn package
java -jar target/ContentModerationEngine-1.0.0.jar
```

### Run All Tests
```bash
mvn test
```

---

## Test Coverage Summary

| Test Class                        | Focus                                      | Tests |
|-----------------------------------|--------------------------------------------|-------|
| `SeverityEngineTest`              | HIGH→BLOCK, MEDIUM→FLAG, LOW→WARNING       | 7     |
| `TextMatchingTest`                | Case-insensitive, symbols, partial match   | 11    |
| `StrikeContextTest`               | Strike counts, actions, context rules      | 13    |
| `AuditLoggerTest`                 | Log creation, count, content accuracy      | 11    |
| `ModerationEngineIntegrationTest` | Full pipeline end-to-end                   | 14    |
| **Total**                         |                                            | **56**|
