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
        DOCKER_REGISTRY = 'localhost:5000'
        IMAGE_NAME = 'digital-asset-approval-platform'
        IMAGE_TAG = "1.0.${BUILD_NUMBER}"
        DOCKER_CONTAINER = 'digital-asset-approval-platform'
        DOCKER_HOST_PORT = '8082'
        DOCKER_CONTAINER_PORT = '8080'
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
                        def winWar = WAR_FILE.replace('/', '\\')
                        def winDeployDir = "${TOMCAT_DEPLOY_DIR}".replace('/', '\\')
                        def winWebapps = "${webappsDir}".replace('/', '\\')
                        bat """
                            if not exist "${winWebapps}" mkdir "${winWebapps}"
                            if not exist "${winDeployDir}\\${params.DEPLOY_ENV}" mkdir "${winDeployDir}\\${params.DEPLOY_ENV}"
                            copy /Y "${winWar}" "${winWebapps}\\${params.TOMCAT_CONTEXT_PATH}.war"
                            copy /Y "${winWar}" "${winDeployDir}\\${params.DEPLOY_ENV}\\"
                            powershell -Command "Start-Sleep -Seconds 15; for (\$i=0; \$i -lt 30; \$i++) { try { \$r = Invoke-WebRequest -Uri 'http://${params.TOMCAT_HOST}:${params.TOMCAT_PORT}/${params.TOMCAT_CONTEXT_PATH}/api/health' -UseBasicParsing -TimeoutSec 3; if (\$r.StatusCode -eq 200 -and \$r.Content -like '*running*') { Write-Host 'Tomcat context is healthy and ready.'; exit 0 } } catch {}; Start-Sleep -Seconds 2 }; Write-Host 'Proceeding...'; exit 0"
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
                    echo 'CRITICAL: Selenium E2E tests failed on Tomcat! Aborting pipeline to block Production release, Docker, and Ansible stages.'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "=== Stage 8: Docker Image Build (${IMAGE_NAME}:${IMAGE_TAG}) ==="
                script {
                    if (isUnix()) {
                        sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                    } else {
                        bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                    }
                }
            }
        }

        stage('Docker Tag') {
            steps {
                echo "=== Stage 9: Docker Image Tagging ==="
                script {
                    if (isUnix()) {
                        sh """
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                        """
                    } else {
                        bat """
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                        """
                    }
                }
            }
        }

        stage('Docker Push') {
            steps {
                echo "=== Stage 10: Docker Push to Local Registry (${DOCKER_REGISTRY}) ==="
                script {
                    if (isUnix()) {
                        sh """
                            docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                        """
                    } else {
                        bat """
                            docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                        """
                    }
                }
            }
        }

        stage('Docker Deploy') {
            steps {
                echo "=== Stage 11: Deploy Fresh Docker Container (${DOCKER_CONTAINER}) ==="
                script {
                    if (isUnix()) {
                        sh """
                            docker stop ${DOCKER_CONTAINER} || true
                            docker rm ${DOCKER_CONTAINER} || true
                            docker pull ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker run -d --name ${DOCKER_CONTAINER} -p ${DOCKER_HOST_PORT}:${DOCKER_CONTAINER_PORT} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        """
                        sh """
                            sleep 5
                            for i in \$(seq 1 30); do
                                if curl -s -f http://localhost:${DOCKER_HOST_PORT}/api/health | grep -q 'running'; then
                                    echo "Fresh Docker container is healthy and responding."
                                    break
                                fi
                                sleep 2
                            done
                            docker ps -f name=${DOCKER_CONTAINER}
                            docker logs --tail 30 ${DOCKER_CONTAINER}
                        """
                    } else {
                        bat """
                            powershell -Command "try { docker stop ${DOCKER_CONTAINER} 2>&1 | Out-Null } catch {}; try { docker rm ${DOCKER_CONTAINER} 2>&1 | Out-Null } catch {}"
                            docker pull ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker run -d --name ${DOCKER_CONTAINER} -p ${DOCKER_HOST_PORT}:${DOCKER_CONTAINER_PORT} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            powershell -Command "Start-Sleep -Seconds 5; for (\$i=0; \$i -lt 30; \$i++) { try { \$r = Invoke-WebRequest -Uri 'http://localhost:${DOCKER_HOST_PORT}/api/health' -UseBasicParsing -TimeoutSec 3; if (\$r.StatusCode -eq 200 -and \$r.Content -like '*running*') { Write-Host 'Fresh Docker container is healthy and responding.'; exit 0 } } catch {}; Start-Sleep -Seconds 2 }; Write-Error 'Docker container health check failed on port ${DOCKER_HOST_PORT}'; exit 1"
                            docker ps --filter "name=${DOCKER_CONTAINER}"
                            docker logs --tail 30 ${DOCKER_CONTAINER}
                        """
                    }
                }
            }
        }

        stage('Production Promotion') {
            when {
                allOf {
                    expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
                    expression { params.DEPLOY_ENV == 'production' }
                }
            }
            steps {
                echo '=== Stage 12: Production Promotion Gate Passed ==='
                script {
                    echo "Quality gates passed. Promoting verified WAR artifact to production release location..."
                    def prodDir = "${TOMCAT_DEPLOY_DIR}/production"
                    if (isUnix()) {
                        sh """
                            mkdir -p "${prodDir}"
                            cp -f ${WAR_FILE} "${prodDir}/"
                        """
                    } else {
                        def winWar = WAR_FILE.replace('/', '\\')
                        def winProdDir = "${prodDir}".replace('/', '\\')
                        bat """
                            if not exist "${winProdDir}" mkdir "${winProdDir}"
                            copy /Y "${winWar}" "${winProdDir}\\"
                        """
                    }
                    echo "Promoted ${WAR_FILE} to ${prodDir} as verified production release."
                }
            }
        }

        stage('Ansible Provisioning') {
            steps {
                echo "=== Stage 13: Ansible Configuration Management & Provisioning (${IMAGE_TAG}) ==="
                script {
                    if (isUnix()) {
                        sh """
                            docker run --rm --network daap-net \
                              -v "\${WORKSPACE}/ansible:/ansible" \
                              -v "\${WORKSPACE}/target:/target" \
                              -e APP_VERSION="${IMAGE_TAG}" \
                              daap-ansible-img -i inventory.ini site.yml
                        """
                    } else {
                        def winWs = "${WORKSPACE}".replace('/', '\\')
                        bat """
                            docker run --rm --network daap-net -v "${winWs}\\ansible:/ansible" -v "${winWs}\\target:/target" -e APP_VERSION=${IMAGE_TAG} daap-ansible-img -i inventory.ini site.yml
                        """
                    }
                }
            }
        }

        stage('Ansible Health Check') {
            steps {
                echo "=== Stage 14: Automated Health Verification on Target Node (Port 8083) ==="
                script {
                    if (isUnix()) {
                        sh """
                            curl -s -f http://localhost:8083/api/health | grep 'running'
                        """
                    } else {
                        bat """
                            powershell -Command "for (\$i=0; \$i -lt 30; \$i++) { try { \$r = Invoke-WebRequest -Uri 'http://localhost:8083/api/health' -UseBasicParsing -TimeoutSec 3; if (\$r.StatusCode -eq 200 -and \$r.Content -like '*running*') { Write-Host 'Ansible target node is healthy and responding.'; exit 0 } } catch {}; Start-Sleep -Seconds 2 }; Write-Error 'Ansible target health check failed on port 8083'; exit 1"
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS: Build, Tests, Packaging, Tomcat Deployment, Selenium Verification, Docker CI/CD, Production Promotion, and Ansible Provisioning completed."
        }
        failure {
            echo "Pipeline FAILURE: Build stopped on error. Inspect surefire reports, selenium-screenshots, docker logs, or ansible output."
        }
    }
}
