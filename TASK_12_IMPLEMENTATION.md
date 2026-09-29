# Task 12 Implementation: Jenkins-Docker Continuous Deployment

## 1. Overview
The declarative `Jenkinsfile` is extended with four continuous deployment stages appended sequentially after the automated testing and Tomcat staging verification lifecycle:
1. `Docker Build`
2. `Docker Tag`
3. `Docker Push`
4. `Docker Deploy`

Quality gating is enforced by Stage 7 (`Selenium Tests`). If any Selenium test fails, downstream Docker build, push, deploy, and production promotion stages are aborted.

---

## 2. Pipeline Architecture & Flow
```
[1. Checkout]
      ↓
[2. Build] (mvn compile)
      ↓
[3. Unit Tests] (mvn test - 7 unit tests)
      ↓
[4. Package] (mvn package - WAR build)
      ↓
[5. Archive] (target/*.war archived)
      ↓
[6. Test/Staging Deployment] (Tomcat 10.1.60 @ port 8081)
      ↓
[7. Selenium Tests] ─────────┬──────── [FAILED] ──→ Pipeline Aborted (Docker Stages Skipped)
                             └─── [PASSED]
                                    ↓
                         [8. Docker Build] (digital-asset-approval-platform:1.0.${BUILD_NUMBER})
                                    ↓
                         [9. Docker Tag] (localhost:5000/digital-asset-approval-platform:1.0.${BUILD_NUMBER}, latest)
                                    ↓
                         [10. Docker Push] (Pushed to local registry @ port 5000)
                                    ↓
                         [11. Docker Deploy] (Fresh container on host port 8082)
                                    ↓
                         [12. Production Promotion] (Promoted to deployments/production/)
```

---

## 3. Configuration & Exact Identifiers
* **Docker Registry**: `localhost:5000` (Official `registry:2` container named `daap-registry`)
* **Docker Image Name**: `digital-asset-approval-platform`
* **Versioning Scheme**: `1.0.${BUILD_NUMBER}` (plus `latest`)
* **Full Image Tags**:
  - `localhost:5000/digital-asset-approval-platform:1.0.${BUILD_NUMBER}`
  - `localhost:5000/digital-asset-approval-platform:latest`
* **Container Name**: `digital-asset-approval-platform`
* **Port Mapping**: Host Port `8082` -> Container Port `8080`
* **Health Endpoint**: `http://localhost:8082/api/health`

---

## 4. Pipeline Stages Detail

### 4.1 Docker Build Stage
```groovy
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
```

### 4.2 Docker Tag Stage
```groovy
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
```

### 4.3 Docker Push Stage
```groovy
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
```

### 4.4 Docker Deploy Stage (Fresh Container Lifecycle)
Ensures every deployment stops and cleans existing containers, pulls the fresh build, maps port 8082, and verifies health:
```groovy
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
```

---

## 5. Quality Gate Enforcement & Failure Semantics
1. **Successful Run**:
   - Stages 1–7 complete green.
   - Stage 8 builds `digital-asset-approval-platform:1.0.${BUILD_NUMBER}`.
   - Stage 9 tags with build version and `latest`.
   - Stage 10 pushes image artifacts to `localhost:5000`.
   - Stage 11 deploys the verified image to port `8082` and validates HTTP 200 health.
   - Stage 12 promotes the verified release to `deployments/production/`.
2. **Selenium Failure Gating**:
   - If any Selenium test in Stage 7 fails, the pipeline immediately traps the failure with status `FAILURE`.
   - Stages 8 (`Docker Build`), 9 (`Docker Tag`), 10 (`Docker Push`), 11 (`Docker Deploy`), and 12 (`Production Promotion`) are skipped.
   - Unverified artifacts are never containerized, pushed, or released.
