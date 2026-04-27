# Content Moderation Engine

> **Exam Topics Demonstrated:** GitHub Collaboration · Maven · JUnit 5 · Jenkins CI/CD

---

## 📐 Project Architecture

```
Input Text
    │
    ▼
TextNormalizer  ── lowercase + leet-decode + strip symbols     (Person 2)
    │
    ▼
TextMatcher     ── find banned word in normalized text          (Person 2)
    │
    ├── No match → ALLOW + log
    │
    ▼
SeverityEngine  ── HIGH→BLOCK | MEDIUM→FLAG | LOW→ALLOW_WITH_WARNING  (Person 1)
    │
    ▼
ContextRuleManager ── EDUCATIONAL/MEDICAL downgrade | GAMING upgrade   (Person 3)
    │
    ▼
StrikeManager   ── 1st→warning | 2nd→temp block | 3rd→permanent block  (Person 3)
    │
    ▼
AuditLogger     ── records user, word, action, severity, timestamp      (Person 4)
    │
    ▼
ModerationResult returned to caller
```

---

## 👥 4-Person Division of Work

| Person | Module | Branch |
|--------|--------|--------|
| **Person 1** | SeverityEngine, WordRepository, model enums | `feature-severity-engine` |
| **Person 2** | TextNormalizer, TextMatcher | `feature-text-matching` |
| **Person 3** | StrikeManager, ContextRuleManager | `feature-strikes-context` |
| **Person 4** | AuditLogger, all JUnit tests | `feature-audit-logs` |

---

## 🖥️ Menu-Driven Program Features

When you run the program, you get a **numbered menu** with these options:

```
 1. Moderate a message
 2. View full audit log
 3. View audit log – filter by User
 4. View audit log – filter by Action
 5. View audit log – filter by Severity
 6. View audit summary statistics
 7. Word dictionary management (add / remove)
 8. View strike board (all users)
 9. Reset a user's strikes
10. Change context
11. Change user
12. Browse banned words by severity
13. Normalization demo (leet-decode live)
14. Exit
```

---

## ⚙️ Maven – Build Commands

All commands must be run from the project root (where `pom.xml` lives).

```bash
# Compile all source code
mvn compile

# Run ALL JUnit tests
mvn test

# Run tests for a specific person's module
mvn test -Dtest=SeverityEngineTest               # Person 1
mvn test -Dtest=TextMatchingTest                 # Person 2
mvn test -Dtest=StrikeContextTest                # Person 3
mvn test -Dtest=AuditLoggerTest                  # Person 4
mvn test -Dtest=ModerationEngineIntegrationTest  # Person 4 integration

# Package into executable JAR
mvn package

# Run the JAR (interactive menu)
java -jar target/ContentModerationEngine.jar

# Full lifecycle (compile + test + package)
mvn verify

# Clean build outputs
mvn clean

# Clean + full build + test in one command
mvn clean verify
```

---

## 🧪 JUnit 5 – Test Structure

| Test Class | Owner | Tests |
|------------|-------|-------|
| `SeverityEngineTest` | Person 1 | HIGH→BLOCK, MEDIUM→FLAG, LOW→WARNING, null→ALLOW, describeAction |
| `TextMatchingTest` | Person 2 | lowercase, leet-speak, symbols, partial match, containsWord |
| `StrikeContextTest` | Person 3 | strike counts, escalation actions, context whitelist/blocklist, resolveAction |
| `AuditLoggerTest` | Person 4 | log creation, field accuracy, query by user/action/severity, clear |
| `ModerationEngineIntegrationTest` | Person 4 | full pipeline end-to-end, all context overrides, strike escalation |

Test results appear in:
- **Console**: `mvn test`
- **Jenkins**: Test Results tab → per-test pass/fail breakdown
- **GitHub Actions**: Annotations on each commit / PR

---

## 🔀 GitHub Collaboration – Step-by-Step

### Initial setup (one person does this)

```bash
# 1. Create the repo on GitHub (named e.g. ContentModerationEngine)
# 2. Clone it locally
git clone https://github.com/YOUR_ORG/ContentModerationEngine.git
cd ContentModerationEngine

# 3. Push the initial project structure on main
git add .
git commit -m "Initial project structure – pom.xml, models, Main.java"
git push origin main
```

### Each person works on their feature branch

```bash
# ── Person 1 ──────────────────────────────────────────────
git checkout -b feature-severity-engine
# ... write SeverityEngine.java, WordRepository.java, SeverityEngineTest.java
git add src/main/java/com/moderation/engine/SeverityEngine.java
git add src/main/java/com/moderation/engine/WordRepository.java
git add src/test/java/com/moderation/SeverityEngineTest.java
git commit -m "feat(severity): implement SeverityEngine and WordRepository

- SeverityEngine maps HIGH→BLOCK, MEDIUM→FLAG, LOW→ALLOW_WITH_WARNING
- WordRepository pre-loaded with 15 default words across 3 severity tiers
- SeverityEngineTest: 10 JUnit 5 tests all passing"
git push origin feature-severity-engine
# → Open Pull Request on GitHub: feature-severity-engine → main

# ── Person 2 ──────────────────────────────────────────────
git checkout -b feature-text-matching
# ... write TextNormalizer.java, TextMatcher.java, TextMatchingTest.java
git add src/main/java/com/moderation/util/
git add src/test/java/com/moderation/TextMatchingTest.java
git commit -m "feat(text): implement TextNormalizer and TextMatcher

- TextNormalizer: lowercase, leet-decode (k1ll→kill, b0mb→bomb), strip symbols
- TextMatcher: case-insensitive, partial-match, leet-speak detection
- TextMatchingTest: 16 JUnit 5 tests all passing"
git push origin feature-text-matching
# → Open Pull Request on GitHub: feature-text-matching → main

# ── Person 3 ──────────────────────────────────────────────
git checkout -b feature-strikes-context
# ... write StrikeManager.java, ContextRuleManager.java, StrikeContextTest.java
git add src/main/java/com/moderation/engine/StrikeManager.java
git add src/main/java/com/moderation/engine/ContextRuleManager.java
git add src/test/java/com/moderation/StrikeContextTest.java
git commit -m "feat(strikes): implement StrikeManager and ContextRuleManager

- StrikeManager: 1st→WARNING, 2nd→TEMP_BLOCK, 3rd+→PERM_BLOCK
- ContextRuleManager: EDUCATIONAL & MEDICAL whitelist, GAMING blocklist
- StrikeContextTest: 18 JUnit 5 tests all passing"
git push origin feature-strikes-context
# → Open Pull Request on GitHub: feature-strikes-context → main

# ── Person 4 ──────────────────────────────────────────────
git checkout -b feature-audit-logs
# ... write AuditLogger.java, all 5 test files
git add src/main/java/com/moderation/log/AuditLogger.java
git add src/test/java/com/moderation/
git commit -m "feat(audit): implement AuditLogger and all JUnit test suites

- AuditLogger: log(), query by user/action/severity, printAll(), printSummary()
- AuditLoggerTest: 14 tests
- ModerationEngineIntegrationTest: 16 end-to-end pipeline tests
- All 5 test classes passing"
git push origin feature-audit-logs
# → Open Pull Request on GitHub: feature-audit-logs → main
```

### Merging / reviewing Pull Requests

Each PR should be:
1. Reviewed by at least one other team member on GitHub
2. Merged only after GitHub Actions CI passes (all JUnit tests green)
3. Merged in order: Person 1 → Person 2 → Person 3 → Person 4

```bash
# After PR is merged into main, each person syncs:
git checkout main
git pull origin main
```

### Final commit on main (Person 1 or team lead)

```bash
git checkout main
git add Jenkinsfile .github/workflows/ci.yml README.md
git commit -m "ci: add Jenkinsfile, GitHub Actions workflow, and README

- Jenkinsfile: declarative pipeline with compile/test/package/verify stages
- ci.yml: GitHub Actions CI with JUnit report publishing
- README: full setup and collaboration guide"
git push origin main
```

---

## 🏗️ Jenkins Setup Guide

### Step 1 – Prerequisites on your Jenkins server

1. Install **JDK 11** and **Maven 3.9** on the Jenkins machine
2. In Jenkins → **Manage Jenkins** → **Global Tool Configuration**:
   - Add JDK named `JDK-11`
   - Add Maven named `Maven-3.9`
3. Install the **Pipeline** plugin (usually pre-installed)
4. Install the **JUnit** plugin (for `junit()` step in Jenkinsfile)

### Step 2 – Create the Pipeline job

1. Jenkins Dashboard → **New Item**
2. Name: `ContentModerationEngine`
3. Type: **Pipeline** → OK
4. Under **Pipeline**:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/YOUR_ORG/ContentModerationEngine.git`
   - Credentials: add your GitHub token if the repo is private
   - Branch Specifier: `*/main`
   - Script Path: `Jenkinsfile`
5. Click **Save** → **Build Now**

### Step 3 – What you will see in Jenkins

```
Pipeline Stages:
  ✅ 1. Checkout           – source code fetched from GitHub
  ✅ 2. Compile            – mvn clean compile
  ✅ 3. JUnit Tests        – mvn test  →  Test Results tab shows all 60+ tests
  ✅ 4. Module Test Summary – 4 parallel stages, one per person
  ✅ 5. Package            – ContentModerationEngine.jar archived
  ✅ 6. Verify             – mvn verify passes
```

The **Test Results** tab inside Jenkins will show every individual JUnit test
with its pass/fail status, execution time, and error messages if any failed.

---

## 📁 Project Structure

```
ContentModerationEngine/
├── pom.xml                                   ← Maven build config + JUnit 5 deps
├── Jenkinsfile                               ← Jenkins declarative pipeline
├── README.md                                 ← This file
├── .github/
│   └── workflows/
│       └── ci.yml                            ← GitHub Actions CI/CD
└── src/
    ├── main/java/com/moderation/
    │   ├── Main.java                         ← Menu-driven interactive program
    │   ├── model/
    │   │   ├── Severity.java                 ← (P1) HIGH / MEDIUM / LOW
    │   │   ├── ModerationAction.java         ← (P1) ALLOW / FLAG / BLOCK / ...
    │   │   ├── Context.java                  ← (P3) GENERAL / EDU / MEDICAL / GAMING
    │   │   ├── BannedWord.java               ← (P1) word + severity
    │   │   ├── ModerationResult.java         ← (P1) result value object
    │   │   └── AuditLog.java                 ← (P4) single log record
    │   ├── engine/
    │   │   ├── ModerationEngine.java         ← Orchestrator (all persons)
    │   │   ├── SeverityEngine.java           ← (P1) severity → action mapping
    │   │   ├── WordRepository.java           ← (P1) banned-word dictionary
    │   │   ├── StrikeManager.java            ← (P3) strike tracking + escalation
    │   │   └── ContextRuleManager.java       ← (P3) context whitelists/blocklists
    │   ├── util/
    │   │   ├── TextNormalizer.java           ← (P2) lowercase + leet-decode
    │   │   └── TextMatcher.java              ← (P2) banned-word detection
    │   └── log/
    │       └── AuditLogger.java              ← (P4) in-memory audit log
    └── test/java/com/moderation/
        ├── SeverityEngineTest.java           ← (P1) 10 tests
        ├── TextMatchingTest.java             ← (P2) 16 tests
        ├── StrikeContextTest.java            ← (P3) 18 tests
        ├── AuditLoggerTest.java              ← (P4) 14 tests
        └── ModerationEngineIntegrationTest.java ← (P4) 16 integration tests
```

---

## 🔒 Severity & Context Quick Reference

### Severity → Default Action

| Severity | Default Action |
|----------|----------------|
| HIGH | BLOCK |
| MEDIUM | FLAG |
| LOW | ALLOW_WITH_WARNING |
| (none) | ALLOW |

### Context Overrides

| Context | Effect |
|---------|--------|
| EDUCATIONAL | kill, violence, drug, bomb, weapon → ALLOW_WITH_WARNING |
| MEDICAL | drug, overdose, death, poison, suicide → ALLOW_WITH_WARNING |
| GAMING | cheat, hack, exploit, glitch → always BLOCK |
| GENERAL | No overrides; standard rules apply |

### Strike Escalation

| Strike | Action |
|--------|--------|
| 1st | ALLOW_WITH_WARNING |
| 2nd | TEMPORARY_BLOCK |
| 3rd+ | PERMANENT_BLOCK |
