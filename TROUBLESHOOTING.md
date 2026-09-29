# Production Troubleshooting Guide

This guide details common operational challenges encountered across the CI/CD and deployment pipeline for the Digital Asset Approval Platform (DAAP), along with verified remediations.

---

## 1. Maven Build & Compilation Failures
* **Symptom**: `mvn compile` or `mvn test` fails with `java.lang.UnsupportedClassVersionError` or command not found.
* **Cause**: `mvn` binary not in current shell `PATH`, or JDK version is below 17 (Spring Boot 3 requires Java 17+).
* **Fix**: Ensure `JAVA_HOME` points to JDK 17 (`C:\Program Files\Java\jdk-17`) and add Maven's bin directory to `PATH`.

---

## 2. Selenium / Tomcat Timing Race Conditions
* **Symptom**: Selenium tests fail during Jenkins execution with `TimeoutException` attempting to connect to `http://localhost:8081/digital-asset-approval-platform`.
* **Cause**: Tomcat WAR auto-deployment is asynchronous; running tests immediately after copying WAR results in transient HTTP 404 or connection resets.
* **Fix**: In `Jenkinsfile` Stage 6, introduce a polling readiness loop (`Invoke-WebRequest` to `/api/health` with retry up to 30 times with 2-second delays) before proceeding to Selenium tests.

---

## 3. Jenkins Pipeline Failures & Permission Issues
* **Symptom**: Jenkins job fails with permission denied or cannot execute batch commands.
* **Cause**: Jenkins runs under a service account lacking rights to Docker pipe or directory structures.
* **Fix**: Run Jenkins under the designated developer account (`prash`) with environment variables (`JAVA_HOME`, `PATH`) declared in Jenkins system configuration (`Manage Jenkins > System`).

---

## 4. Docker Build Failures
* **Symptom**: `docker build` fails with `COPY failed: file not found in build context: target/*.war`.
* **Cause**: Build run before `mvn package` was executed, or `.dockerignore` incorrectly excluded `target/`.
* **Fix**: Ensure `.dockerignore` excludes temporary folders while preserving `target/*.war`, and guarantee `mvn package -DskipTests` precedes `docker build`.

---

## 5. Local Docker Registry Failures
* **Symptom**: `docker push localhost:5000/...` fails with `connection refused` or `server gave HTTP response to HTTPS client`.
* **Cause**: `daap-registry` container is stopped or listening on another port.
* **Fix**: Verify registry status with `docker ps --filter "name=daap-registry"`. If stopped, restart with `docker start daap-registry` or recreate with `docker run -d --restart unless-stopped --name daap-registry -p 5000:5000 registry:2`. Docker considers `localhost` an insecure registry by default.

---

## 6. Container Port Conflicts
* **Symptom**: `docker run` fails with `bind: address already in use: 0.0.0.0:8082` (or `8081`, `8083`).
* **Cause**: Existing container or daemon process already occupying the host port.
* **Fix**:
  - Check listening processes: `Get-NetTCPConnection -LocalPort <port>`
  - Stop previous conflicting container: `docker stop <container_name> && docker rm <container_name>`.

---

## 7. Ansible Connection Failures
* **Symptom**: `ansible-playbook` fails with `UNREACHABLE: Connection refused` or `Host key verification failed`.
* **Cause**: Target node SSH daemon not running, or container not attached to the same Docker bridge network (`daap-net`).
* **Fix**: Pass `-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null` in `inventory.ini` and verify that both the controller and target container share `--network daap-net`.

---

## 8. Ansible Package Installation Failures
* **Symptom**: Task `apt: name=...` fails with `Unable to locate package` or mirror 404.
* **Cause**: Outdated apt package index cache in ephemeral container.
* **Fix**: Ensure task `apt: update_cache=yes cache_valid_time=3600` precedes package installation tasks.

---

## 9. Application Health Check Failure
* **Symptom**: Ansible `uri` task or Jenkins curl fails with HTTP 500 or timeout on `/api/health`.
* **Cause**: Spring Boot application failed to start inside container due to port clash or invalid Java options.
* **Fix**: Inspect application log at `/opt/daap/logs/app.log` (or `docker logs digital-asset-approval-platform`) to diagnose Spring context initialization stack traces.

---

## 10. Automated Rollback Failure
* **Symptom**: `ansible-playbook rollback.yml` fails stating release directory does not exist.
* **Cause**: The specified rollback version was never deployed or was cleaned up.
* **Fix**: Verify available release directories with `docker exec daap-target ls -la /opt/daap/releases/` and supply a valid version via `-e "rollback_version=<valid_version>"`.
