/*
 * ================================================================
 * Jenkinsfile – Content Moderation Engine
 * ================================================================
 * Exam Topics Demonstrated:
 *   ✅ Jenkins  – declarative pipeline with stages
 *   ✅ Maven    – compile / test / package / verify
 *   ✅ JUnit    – junit() result publisher built into Jenkins
 *   ✅ GitHub   – SCM polling + branch-based builds
 *
 * How to use:
 *   1. In Jenkins → New Item → Pipeline
 *   2. Pipeline Definition → "Pipeline script from SCM"
 *   3. SCM → Git → Repository URL (your GitHub repo)
 *   4. Script Path → Jenkinsfile
 *   5. Save → Build Now
 * ================================================================
 */

pipeline {

    agent any   // Run on any available Jenkins agent

    // ── Tool configuration ────────────────────────────────────────────
    tools {
    maven 'Maven'
    jdk   'JDK'
}

    // ── Environment variables ─────────────────────────────────────────
    environment {
        APP_NAME    = 'ContentModerationEngine'
        JAR_NAME    = 'ContentModerationEngine.jar'
        MAVEN_OPTS  = '-Xmx512m'
    }

    // ── Trigger: poll GitHub every 5 minutes for new commits ──────────
    triggers {
        pollSCM('H/5 * * * *')
    }

    // ═════════════════════════════════════════════════════════════════
    //  STAGES
    // ═════════════════════════════════════════════════════════════════
    stages {

        // ── Stage 1: Checkout ─────────────────────────────────────────
        stage('1. Checkout') {
            steps {
                echo "=== Checking out branch: ${env.BRANCH_NAME} ==="
                checkout scm
            }
        }

        // ── Stage 2: Compile ──────────────────────────────────────────
        stage('2. Compile') {
            steps {
                echo '=== Compiling source code with Maven ==='
                bat 'mvn clean compile -B'
            }
        }

        // ── Stage 3: Run JUnit Tests ──────────────────────────────────
        stage('3. JUnit Tests') {
            steps {
                echo '=== Running JUnit 5 tests via Maven Surefire ==='
                bat 'mvn test -B'
            }
            post {
                always {
                    // Publish JUnit XML reports inside Jenkins UI
                    junit 'target/surefire-reports/TEST-*.xml'
                    echo '=== JUnit test results published to Jenkins ==='
                }
                failure {
                    echo '❌ One or more tests FAILED. Check the Test Results tab.'
                }
                success {
                    echo '✅ All tests PASSED.'
                }
            }
        }

        // ── Stage 4: Person-by-person module test summary ─────────────
        stage('4. Module Test Summary') {
            parallel {

                stage('Person 1 – SeverityEngineTest') {
                    steps {
                        bat 'mvn test -Dtest=SeverityEngineTest -B'
                    }
                }

                stage('Person 2 – TextMatchingTest') {
                    steps {
                        bat 'mvn test -Dtest=TextMatchingTest -B'
                    }
                }

                stage('Person 3 – StrikeContextTest') {
                    steps {
                        bat 'mvn test -Dtest=StrikeContextTest -B'
                    }
                }

                stage('Person 4 – AuditLoggerTest + Integration') {
                    steps {
                        bat 'mvn test -Dtest=AuditLoggerTest,ModerationEngineIntegrationTest -B'
                    }
                }
            }
        }

        // ── Stage 5: Package JAR ──────────────────────────────────────
        stage('5. Package') {
            steps {
                echo '=== Packaging executable JAR ==='
                sh 'mvn package -DskipTests -B'
                echo "=== JAR created: target/${JAR_NAME} ==="
            }
            post {
                success {
                    archiveArtifacts artifacts: "target/${JAR_NAME}",
                                     fingerprint: true
                    echo '📦 JAR archived as Jenkins build artifact.'
                }
            }
        }

        // ── Stage 6: Verify (full lifecycle check) ────────────────────
        stage('6. Verify') {
            steps {
                echo '=== Running mvn verify (compile + test + package) ==='
                sh 'mvn verify -B'
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  POST-BUILD ACTIONS
    // ═════════════════════════════════════════════════════════════════
    post {

        always {
            echo "=== Build complete for branch: ${env.BRANCH_NAME} ==="
        }

        success {
            echo """
╔══════════════════════════════════════════════╗
║  ✅  BUILD SUCCESSFUL                        ║
║  All JUnit tests passed.                     ║
║  JAR artifact ready for deployment.          ║
╚══════════════════════════════════════════════╝
"""
        }

        failure {
            echo """
╔══════════════════════════════════════════════╗
║  ❌  BUILD FAILED                            ║
║  Check the JUnit Test Results tab above.     ║
╚══════════════════════════════════════════════╝
"""
        }

        unstable {
            echo '⚠️  Build is UNSTABLE – some tests failed. Check the Test Results tab.'
        }
    }
}
