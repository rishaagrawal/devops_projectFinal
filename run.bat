@echo off
echo ========================================
echo  Content Moderation Engine - Runner
echo ========================================
echo.

REM Create output folder for compiled classes
if not exist out mkdir out

REM Compile ALL .java files together
echo [1/2] Compiling all source files...
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
    echo.
    echo [ERROR] Compilation failed. Fix the errors above and try again.
    pause
    exit /b 1
)

echo [2/2] Launching app...
echo.
java -cp out com.moderation.Main

pause