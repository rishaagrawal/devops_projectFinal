@echo off
echo ========================================
echo  Content Moderation Engine - JUnit Tests
echo ========================================
echo.

REM Check JUnit JAR exists
if not exist lib\junit-platform-console-standalone.jar (
    echo [ERROR] JUnit JAR not found!
    echo Please put junit-platform-console-standalone.jar inside the lib\ folder.
    pause
    exit /b 1
)

REM Create output folders
if not exist out      mkdir out
if not exist test-out mkdir test-out

REM --- Step 1: Compile main sources ---
echo [1/3] Compiling main sources...
javac -d out ^
  src\main\java\com\moderation\model\Severity.java ^
  src\main\java\com\moderation\model\ModerationAction.java ^
  src\main\java\com\moderation\model\Context.java ^
  src\main\java\com\moderation\model\BannedWord.java ^
  src\main\java\com\moderation\model\AuditLog.java ^
  src\main\java\com\moderation\model\ModerationResult.java ^
  src\main\java\com\moderation\util\TextNormalizer.java ^
  src\main\java\com\moderation\util\TextMatcher.java ^
  src\main\java\com\moderation\log\AuditLogger.java ^
  src\main\java\com\moderation\engine\SeverityEngine.java ^
  src\main\java\com\moderation\engine\WordRepository.java ^
  src\main\java\com\moderation\engine\StrikeManager.java ^
  src\main\java\com\moderation\engine\ContextRuleManager.java ^
  src\main\java\com\moderation\engine\ModerationEngine.java ^
  src\main\java\com\moderation\Main.java

IF ERRORLEVEL 1 (
    echo [ERROR] Main sources failed to compile!
    pause
    exit /b 1
)

REM --- Step 2: Compile test sources ---
echo [2/3] Compiling test sources...
javac -cp out;lib\junit-platform-console-standalone.jar -d test-out ^
  src\test\java\com\moderation\SeverityEngineTest.java ^
  src\test\java\com\moderation\TextMatchingTest.java ^
  src\test\java\com\moderation\StrikeContextTest.java ^
  src\test\java\com\moderation\AuditLoggerTest.java ^
  src\test\java\com\moderation\ModerationEngineIntegrationTest.java

IF ERRORLEVEL 1 (
    echo [ERROR] Test sources failed to compile!
    pause
    exit /b 1
)

REM --- Step 3: Run JUnit tests ---
echo [3/3] Running all JUnit tests...
echo.
java -jar lib\junit-platform-console-standalone.jar ^
     --class-path out;test-out ^
     --select-class com.moderation.SeverityEngineTest ^
     --select-class com.moderation.TextMatchingTest ^
     --select-class com.moderation.StrikeContextTest ^
     --select-class com.moderation.AuditLoggerTest ^
     --select-class com.moderation.ModerationEngineIntegrationTest

echo.
echo ========================================
echo  All tests complete!
echo ========================================
pause