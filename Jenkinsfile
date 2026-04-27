pipeline {

    agent any

    tools {
        maven 'Maven'
        jdk   'JDK'
    }

    environment {
        APP_NAME   = 'ContentModerationEngine'
        JAR_NAME   = 'ContentModerationEngine.jar'
        MAVEN_OPTS = '-Xmx512m'
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {

        stage('1. Checkout') {
            steps {
                echo "=== Checking out code ==="
                checkout scm
            }
        }

        stage('2. Compile') {
            steps {
                echo '=== Compiling source code ==='
                bat 'mvn clean compile -B'
            }
        }

        stage('3. JUnit Tests') {
            steps {
                echo '=== Running JUnit Tests ==='
                bat 'mvn test -B'
            }
            post {
                always {
                    junit 'target/surefire-reports/TEST-*.xml'
                    echo '=== Test results published ==='
                }
                success {
                    echo '✅ All tests PASSED'
                }
                failure {
                    echo '❌ Tests FAILED'
                }
            }
        }

        stage('4. Module Test Summary') {
            parallel {

                stage('SeverityEngineTest') {
                    steps {
                        bat 'mvn test -Dtest=SeverityEngineTest -B'
                    }
                }

                stage('TextMatchingTest') {
                    steps {
                        bat 'mvn test -Dtest=TextMatchingTest -B'
                    }
                }

                stage('StrikeContextTest') {
                    steps {
                        bat 'mvn test -Dtest=StrikeContextTest -B'
                    }
                }

                stage('Audit + Integration') {
                    steps {
                        bat 'mvn test -Dtest=AuditLoggerTest,ModerationEngineIntegrationTest -B'
                    }
                }
            }
        }

        stage('5. Package') {
            steps {
                echo '=== Packaging JAR ==='
                bat 'mvn package -DskipTests -B'   // ✅ FIXED
                echo "JAR created at target/${JAR_NAME}"
            }
            post {
                success {
                    archiveArtifacts artifacts: "target/${JAR_NAME}", fingerprint: true
                    echo '📦 JAR archived'
                }
            }
        }

        stage('6. Verify') {
            steps {
                echo '=== Running mvn verify ==='
                bat 'mvn verify -B'   // ✅ FIXED
            }
        }
    }

    post {

        always {
            echo "=== Build Completed ==="
        }

        success {
            echo "✅ BUILD SUCCESSFUL"
        }

        failure {
            echo "❌ BUILD FAILED"
        }

        unstable {
            echo "⚠️ BUILD UNSTABLE"
        }
    }
}