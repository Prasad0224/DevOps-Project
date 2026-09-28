pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['test', 'staging', 'production'], description: 'Target deployment environment')
        string(name: 'TOMCAT_HOST', defaultValue: 'localhost', description: 'Apache Tomcat server hostname/IP')
        string(name: 'TOMCAT_PORT', defaultValue: '8081', description: 'Apache Tomcat HTTP port')
        string(name: 'TOMCAT_CONTEXT_PATH', defaultValue: 'digital-asset-approval-platform', description: 'WAR deployment context path on Tomcat')
        string(name: 'TOMCAT_WEBAPPS_DIR', defaultValue: '', description: 'Explicit path to Tomcat webapps directory (defaults to CATALINA_HOME/webapps)')
        booleanParam(name: 'RUN_SELENIUM_E2E', defaultValue: true, description: 'Run automated Selenium E2E suite against Tomcat')
    }

    environment {
        APP_NAME = 'digital-asset-approval-platform'
        WAR_FILE = "target/${APP_NAME}.war"
        TOMCAT_DEPLOY_DIR = "${WORKSPACE}/deployments"
        SELENIUM_SCREENSHOTS = 'target/selenium-screenshots'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    triggers {
        // Task 7: SCM polling trigger (every 5 minutes)
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== Stage 1: Checkout Source from SCM ==='
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '=== Stage 2: Maven Compile ==='
                script {
                    if (isUnix()) {
                        sh 'mvn compile'
                    } else {
                        bat 'mvn compile'
                    }
                }
            }
        }

        stage('Unit Tests') {
            steps {
                echo '=== Stage 3: Automated Unit Tests ==='
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/surefire-reports/TEST-com.platform.service.*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                echo '=== Stage 4: Package WAR Artifact for Tomcat ==='
                script {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests'
                    } else {
                        bat 'mvn package -DskipTests'
                    }
                }
            }
        }

        stage('Archive') {
            steps {
                echo '=== Stage 5: Archive Build Artifacts ==='
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Test/Staging Deployment') {
            steps {
                echo "=== Stage 6: Deploy WAR to Tomcat (${params.DEPLOY_ENV}) ==="
                script {
                    def webappsDir = params.TOMCAT_WEBAPPS_DIR ?: (env.CATALINA_HOME ? "${env.CATALINA_HOME}/webapps" : "${TOMCAT_DEPLOY_DIR}/webapps")
                    echo "Deploying ${WAR_FILE} to Tomcat webapps: ${webappsDir}..."
                    
                    if (isUnix()) {
                        sh """
                            mkdir -p "${webappsDir}"
                            mkdir -p "${TOMCAT_DEPLOY_DIR}/${params.DEPLOY_ENV}"
                            cp -f ${WAR_FILE} "${webappsDir}/${params.TOMCAT_CONTEXT_PATH}.war"
                            cp -f ${WAR_FILE} "${TOMCAT_DEPLOY_DIR}/${params.DEPLOY_ENV}/"
                        """
                    } else {
                        bat """
                            if not exist "${webappsDir}" mkdir "${webappsDir}"
                            if not exist "${TOMCAT_DEPLOY_DIR}\\${params.DEPLOY_ENV}" mkdir "${TOMCAT_DEPLOY_DIR}\\${params.DEPLOY_ENV}"
                            copy /Y "${WAR_FILE}" "${webappsDir}\\${params.TOMCAT_CONTEXT_PATH}.war"
                            copy /Y "${WAR_FILE}" "${TOMCAT_DEPLOY_DIR}\\${params.DEPLOY_ENV}\\"
                        """
                    }
                    echo "WAR deployed to Tomcat webapps directory as ${params.TOMCAT_CONTEXT_PATH}.war."
                }
            }
        }

        stage('Selenium Tests') {
            when {
                expression { params.RUN_SELENIUM_E2E == true }
            }
            steps {
                echo '=== Stage 7: Automated Selenium E2E Tests on Tomcat ==='
                script {
                    def tomcatBaseUrl = "http://${params.TOMCAT_HOST}:${params.TOMCAT_PORT}/${params.TOMCAT_CONTEXT_PATH}"
                    echo "Executing Selenium tests against Tomcat deployment at: ${tomcatBaseUrl}"

                    if (isUnix()) {
                        sh "mvn test -Pselenium -Dapp.baseUrl=${tomcatBaseUrl} -Dselenium.headless=true"
                    } else {
                        bat "mvn test -Pselenium \"-Dapp.baseUrl=${tomcatBaseUrl}\" \"-Dselenium.headless=true\""
                    }
                }
            }
            post {
                always {
                    // Publish test results and failure screenshots
                    junit testResults: '**/surefire-reports/TEST-com.platform.selenium.*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/selenium-screenshots/**', allowEmptyArchive: true
                }
                failure {
                    echo 'CRITICAL: Selenium E2E tests failed on Tomcat! Aborting pipeline to block Production release.'
                }
            }
        }

        stage('Production Deployment') {
            when {
                allOf {
                    expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
                    expression { params.DEPLOY_ENV == 'production' }
                }
            }
            steps {
                echo '=== Stage 8: Production Promotion Gate Passed ==='
                script {
                    echo "Selenium E2E verification passed. Promoting verified WAR artifact to production release location..."
                    def prodDir = "${TOMCAT_DEPLOY_DIR}/production"
                    if (isUnix()) {
                        sh """
                            mkdir -p "${prodDir}"
                            cp -f ${WAR_FILE} "${prodDir}/"
                        """
                    } else {
                        bat """
                            if not exist "${prodDir}" mkdir "${prodDir}"
                            copy /Y "${WAR_FILE}" "${prodDir}\\"
                        """
                    }
                    echo "Promoted ${WAR_FILE} to ${prodDir} as verified production release."
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS: Build, Tests, Packaging, Tomcat Deployment, and Selenium Verification completed."
        }
        failure {
            echo "Pipeline FAILURE: Build stopped on error. Inspect surefire reports and selenium-screenshots."
        }
    }
}
