# Automated Digital Asset Approval Platform (DAAP) — DevOps Documentation

**Repository**: [https://github.com/Prasad0224/DevOps-Project.git](https://github.com/Prasad0224/DevOps-Project.git)  
**Branch**: `develop`  
**Overall Status**: **VERIFIED / SUBMISSION READY**

---

## 1. Project Overview
The **Automated Digital Asset Approval Platform (DAAP)** is an enterprise-grade digital asset governance portal designed to streamline submission, validation, review, and auditable approval workflows for enterprise digital media. It replaces ad-hoc, error-prone manual approvals with an automated web platform backed by a robust Continuous Integration, Continuous Delivery, Containerization, and Configuration Management engineering pipeline.

---

## 2. Architecture
The system integrates application code, test automation, containerization, and configuration management across clearly isolated ports:

```
[Developer / Git Client]
          │ (Push to origin/develop)
          ▼
[GitHub Repository] ◄─── (SCM Polling H/5) ───┐
                                               ▼
                                      [Jenkins Orchestrator] (Port 8080)
                                               │
               ┌───────────────────────────────┴───────────────────────────────┐
               ▼                                                               ▼
   [Apache Tomcat 10.1.60] (Port 8081)                         [Docker Engine & Local Registry]
   - WAR Staging Deployment                                    - Registry (Port 5000)
   - Verified via Selenium E2E (Port 8081)                     - App Container (Port 8082 -> 8080)
               │                                                               │
               └───────────────────────────────┬───────────────────────────────┘
                                               ▼
                                  [Ansible Provisioning]
                                  - Controller: daap-ansible-img
                                  - Target Node: daap-target (Port 8083)
                                  - Supervisor Daemon & Atomic Symlinks
```

### Port Allocation:
* **Jenkins Automation Server**: `8080`
* **Apache Tomcat 10.1.60 (Staging)**: `8081` (`/digital-asset-approval-platform`)
* **Docker Container Application**: `8082` (Container port `8080`)
* **Ansible Target Host (`daap-target`)**: `8083` (Managed by `supervisor`)
* **Local Docker Registry**: `5000` (`daap-registry`)

---

## 3. Tasks 1–6 Summary
The engineering foundation established across Tasks 1 through 6 encompasses:
* **Task 1 (Problem & Scope)**: Defined role segregation between Requesters and Reviewers with core submission constraints (files <= 10MB; formats: jpg, png, pdf, docx).
* **Task 2 (Agile & Backlog)**: User stories US-01 through US-06 prioritized in `docs/backlog.md` with Git branching model (`feature/*`, `bugfix/*`, `release/*`, `develop`, `main`).
* **Task 3 (Core Application & UI)**: Built on Java 17 LTS and Spring Boot 3.1.2 with `SpringBootServletInitializer` for WAR compatibility. Responsive web UI in `src/main/resources/static/index.html`.
* **Task 4 (Version Control & Issues)**: Standardized linear Git history, GitHub issue templates in `.github/ISSUE_TEMPLATE/` (`bug_report.md`, `feature_request.md`), and contribution guidelines.
* **Task 5 (Validation & Testing)**: Service-layer input and file constraint validation covered by automated unit tests in `AssetRequestServiceTest.java`.
* **Task 6 (Review Workflow & Audit)**: Implementation of `APPROVED`, `REJECTED`, and `CHANGES_REQUESTED` state transitions with immutable audit trails (`AuditLogRepository`). 7/7 unit tests verified.

---

## 4. Jenkins CI/CD
Continuous Integration is orchestrated by Jenkins 2.568.3 via the root declarative [Jenkinsfile](../Jenkinsfile):
* **SCM Polling**: Configured with `pollSCM('H/5 * * * *')` tracking `refs/heads/develop`.
* **Automated Compilation**: `mvn compile` validates syntax and compiles bytecode.
* **Test Reporting**: Surefire test results published automatically to Jenkins via `junit`.
* **Artifact Archiving**: Web application archive `target/digital-asset-approval-platform.war` fingerprinted and archived each build.
* **Parameterization**: Supports `DEPLOY_ENV` (`test`, `staging`, `production`), `TOMCAT_HOST`, `TOMCAT_PORT` (`8081`), and `RUN_SELENIUM_E2E`.

---

## 5. Tomcat Deployment
* **Servlet Container**: Apache Tomcat 10.1.60 on HTTP port `8081` (configured via `conf/server.xml` to avoid port 8080 collision with Jenkins).
* **Packaging**: Standard WAR produced via `<packaging>war</packaging>` with `spring-boot-starter-tomcat` under `<scope>provided</scope>`.
* **Deployment Mechanism**: Stage 6 copies `digital-asset-approval-platform.war` directly to `<TOMCAT_HOME>/webapps/`.
* **Context Readiness**: Jenkins polls `http://localhost:8081/digital-asset-approval-platform/api/health` with retry loop to guarantee container initialization before running downstream stages.

---

## 6. Selenium Testing
Automated browser regression is performed using Selenium WebDriver 4.16.1 in headless Google Chrome:
* **Suite File**: `src/test/java/com/platform/selenium/AssetApprovalSeleniumTest.java`
* **Test Suite**: 5 E2E tests:
  1. `test1_AssetSubmissionSuccess`: Valid asset submission and queue display.
  2. `test2_AssetSubmissionValidationFailure`: Form rejection on invalid inputs.
  3. `test3_DashboardDataRender`: Dashboard metrics and queue rendering.
  4. `test4_ApproveAssetWorkflow`: Reviewer approval state transition to `APPROVED`.
  5. `test5_RejectAssetWorkflow`: Reviewer rejection state transition to `REJECTED`.
* **Quality Gate**: Executed against Tomcat staging. Any failure aborts subsequent pipeline stages and triggers automated failure screenshot capture to `target/selenium-screenshots/`.

---

## 7. Docker Image and Container Lifecycle
* **Dockerfile**: Based on `eclipse-temurin:17-jre`:
  ```dockerfile
  FROM eclipse-temurin:17-jre
  WORKDIR /app
  COPY target/digital-asset-approval-platform.war app.war
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.war"]
  ```
* **Image Tagging**: `digital-asset-approval-platform:1.0.${BUILD_NUMBER}` and `latest`.
* **Container Run Command**:
  ```bash
  docker run -d --name digital-asset-approval-platform -p 8082:8080 digital-asset-approval-platform:1.0.0
  ```
* **Port Mapping**: Host Port `8082` -> Container Port `8080`.
* **Lifecycle Operations**: Full support for `docker build`, `docker tag`, `docker run`, `docker stop`, `docker restart`, and `docker rm`.

---

## 8. Local Docker Registry
* **Registry Container**: Official `registry:2` container named `daap-registry` on port `5000`.
* **Setup Command**:
  ```bash
  docker run -d --restart unless-stopped --name daap-registry -p 5000:5000 registry:2
  ```
* **Image Tags Pushed**:
  - `localhost:5000/digital-asset-approval-platform:1.0.${BUILD_NUMBER}`
  - `localhost:5000/digital-asset-approval-platform:latest`
* **Verification Endpoints**:
  - Catalog: `http://localhost:5000/v2/_catalog` (`repositories: ["digital-asset-approval-platform"]`)
  - Tags: `http://localhost:5000/v2/digital-asset-approval-platform/tags/list`

---

## 9. Jenkins → Docker Continuous Deployment
Continuous deployment stages in `Jenkinsfile` automate the Docker container lifecycle:
* **Stage 8 (Docker Build)**: Builds image tagged with current build number.
* **Stage 9 (Docker Tag)**: Tags image for local registry (`localhost:5000`) and `latest`.
* **Stage 10 (Docker Push)**: Pushes versioned and latest images to local registry.
* **Stage 11 (Docker Deploy)**: Gracefully terminates existing container, pulls fresh image from local registry, launches on host port `8082`, and polls `/api/health` until HTTP 200 is confirmed.

---

## 10. Ansible Configuration Management
Ansible provisions the target environment in an agentless, declarative manner:
* **Ansible Controller**: Containerized in `daap-ansible-img` (`ansible-core` on Ubuntu 22.04).
* **Target Node**: Ubuntu 22.04 container `daap-target` on Docker bridge network `daap-net` with host port mapping `8083:8083`.
* **Playbook (`ansible/site.yml`) Tasks**:
  1. Updates `apt` cache.
  2. Installs `openjdk-17-jre-headless`, `curl`, and `supervisor`.
  3. Creates dedicated system group `daap` and system user `daap` (`nologin` shell).
  4. Creates `/opt/daap/` structure: `releases/`, `config/`, `logs/`.
  5. Deploys versioned WAR to `/opt/daap/releases/{{ app_version }}/app.war`.
  6. Deploys `/opt/daap/config/application.properties` (configured for port `8083`).
  7. Points atomic symlink `/opt/daap/current -> /opt/daap/releases/{{ app_version }}`.
  8. Configures `supervisor` unit `/etc/supervisor/conf.d/daap.conf`.
  9. Starts/reloads `supervisor` and verifies health on port `8083`.

---

## 11. Idempotency Validation
Idempotency ensures running the configuration playbook multiple times produces no unintended modifications:
* **First Run Result**: `ok=17 changed=11 unreachable=0 failed=0` (All components installed and configured).
* **Second Run (Idempotency Proof)**:
  ```
  PLAY RECAP *********************************************************************
  daap-target                : ok=16   changed=0    unreachable=0    failed=0
  ```
* **Verification**: `changed=0` confirms that all packages, users, symlinks, and configuration files were in their exact desired states.

---

## 12. Health Check
Every deployment layer implements automated health verification against `/api/health`:
* **Tomcat Staging**: `http://localhost:8081/digital-asset-approval-platform/api/health` (`HTTP 200 OK`)
* **Docker Container**: `http://localhost:8082/api/health` (`HTTP 200 OK`, body: `"Application is running."`)
* **Ansible Target**: `http://localhost:8083/api/health` (`HTTP 200 OK`, body: `"Application is running."`)

---

## 13. Rollback / Recovery
Atomic rollbacks are managed via `ansible/rollback.yml`:
* **Mechanism**: Verifies that the previous release directory exists and contains an intact WAR, atomically redirects symlink `/opt/daap/current` to the previous release (e.g., `1.0.9`), restarts `supervisor`, and validates `/api/health`.
* **Verified Execution**: Corrupted release `1.1.0-corrupt` was deployed; executing `rollback.yml` restored active release `1.0.9` with `ok=8 changed=2 failed=0` within seconds and verified HTTP 200.

---

## 14. Final End-to-End Pipeline
The 14-stage declarative pipeline executed in Jenkins Build #10:
1. **Checkout**: Source code pulled from `develop`.
2. **Build**: `mvn compile` succeeded.
3. **Unit Tests**: 7/7 unit tests passed; JUnit report generated.
4. **Package**: Produced `digital-asset-approval-platform.war`.
5. **Archive**: WAR fingerprinted and archived.
6. **Test/Staging Deployment**: Deployed to Tomcat 10.1.60 on port 8081.
7. **Selenium Tests**: 5/5 headless Chrome E2E tests passed.
8. **Docker Build**: Built image `digital-asset-approval-platform:1.0.${BUILD_NUMBER}`.
9. **Docker Tag**: Tagged for local registry (`localhost:5000`) and `latest`.
10. **Docker Push**: Pushed images to `localhost:5000`.
11. **Docker Deploy**: Deployed fresh container to port 8082; health verified.
12. **Production Promotion**: Promoted verified WAR to `deployments/production/`.
13. **Ansible Provisioning**: Deployed application to `daap-target` on port 8083 via supervisor.
14. **Ansible Health Check**: Verified HTTP 200 response on `http://localhost:8083/api/health`.

---

## 15. Verified Test Results

| Test Type | Scope | Test Class / Command | Result | Status |
|---|---|---|---|---|
| **Unit Tests** | Service validation, file size, status workflow | `AssetRequestServiceTest.java` | 7/7 Passed (0 fail, 0 err) | **PASS** |
| **Selenium E2E Tests** | UI submission, validation, review, rejection | `AssetApprovalSeleniumTest.java` | 5/5 Passed (0 fail, 0 err) | **PASS** |
| **Total Automated Tests** | Comprehensive test coverage | `mvn test` & `mvn test -Pselenium` | **12/12 Passed** | **PASS** |
| **Jenkins Build #6** | Tasks 7–10 verification | `digital-asset-approval-platform-pipeline` | SUCCESS (1m 25s) | **PASS** |
| **Jenkins Build #9** | Tasks 11–12 verification | `digital-asset-approval-platform-pipeline` | SUCCESS (1m 18s) | **PASS** |
| **Jenkins Build #10** | Tasks 13–15 full pipeline | `digital-asset-approval-platform-pipeline` | SUCCESS | **PASS** |
| **Ansible Run 1** | Fresh node configuration | `site.yml` | ok=17 changed=11 failed=0 | **PASS** |
| **Ansible Run 2** | Idempotency verification | `site.yml` | ok=16 changed=0 failed=0 | **PASS** |
| **Ansible Rollback** | Recovery of release 1.0.9 | `rollback.yml` | ok=8 changed=2 failed=0 | **PASS** |

---

## 16. Evidence / Screenshot Index

### Log Evidence Files:
* [`docs/evidence/ansible_first_run.txt`](evidence/ansible_first_run.txt) — Full output of initial Ansible run (`ok=17 changed=11 failed=0`).
* [`docs/evidence/ansible_idempotency_run.txt`](evidence/ansible_idempotency_run.txt) — Output of second Ansible run (`ok=16 changed=0 failed=0`).
* [`docs/evidence/ansible_health_check.txt`](evidence/ansible_health_check.txt) — HTTP 200 confirmation on `http://localhost:8083/api/health`.
* [`docs/evidence/ansible_rollback_run.txt`](evidence/ansible_rollback_run.txt) — Rollback restoration of release 1.0.9.

### Screenshot Artifacts:
* `docs/screenshots/app_ui_live.png` — Live web application portal on Tomcat 10.1.60 (port 8081).
* `docs/screenshots/docker_app_ui.png` — Live application running in Docker container (port 8082).
* `docs/screenshots/docker_health_check.png` — HTTP 200 health check response for Docker container.
* `docs/screenshots/ansible_target_app.png` — Live application running on Ansible target (port 8083).
* `docs/screenshots/ansible_health.png` — HTTP 200 health check response on Ansible target.
* `docs/screenshots/ansible_first_run.png` — Play recap of initial Ansible run (`changed=11`).
* `docs/screenshots/ansible_idempotency.png` — Play recap of second Ansible run (`changed=0`).
* `docs/screenshots/ansible_rollback.png` — Play recap of automated rollback to 1.0.9.
* `docs/screenshots/jenkins_pipeline_overview.png` — Jenkins dashboard overview of pipeline builds.
* `docs/screenshots/jenkins_build_5_failure_gate.png` — Demonstration of failure gate aborting production release.
* `docs/screenshots/jenkins_build_6_success.png` — Jenkins Build #6 build completion.
* `docs/screenshots/jenkins_build_6_stages.png` — Jenkins Build #6 stage view (all stages green).
* `docs/screenshots/jenkins_build_9_stages.png` — Jenkins Build #9 stage view (Docker stages green).
* `docs/screenshots/jenkins_build_9_console.png` — Jenkins Build #9 console log showing Docker deploy.
* `docs/screenshots/jenkins_final_pipeline.png` — Jenkins final full pipeline execution.

---

## 17. Security Considerations
* **Non-Root Execution**: Container and Ansible target run under non-root system user `daap` with a `nologin` shell.
* **Network Isolation**: Docker registry, Ansible controller, and target interact over private Docker network `daap-net`.
* **Secrets Management**: No credentials, passwords, or private keys committed to source control; `.gitignore` and `.dockerignore` properly enforce exclusions.
* **Input Validation**: Strict client-side and server-side validation on file extensions, file sizes, and metadata strings.

---

## 18. Limitations
Detailed in [LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md](LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md):
* Single-host development execution environment.
* In-memory database persistence (H2 / memory structures).
* Ephemeral local Docker registry (`localhost:5000`) without external cloud replication.
* HTTP transport across internal services rather than end-to-end TLS/HTTPS.

---

## 19. Future Enhancements
* Transition from standalone Docker/Ansible targets to Kubernetes (EKS/GKE) with Helm charts.
* Integration with cloud container registries (AWS ECR, Azure ACR) with Trivy vulnerability scanning.
* Enterprise secrets management via HashiCorp Vault.
* Blue/Green zero-downtime deployment automation.
* Production relational database integration (PostgreSQL) and Prometheus/Grafana monitoring.

---

## 20. Companion Documentation Links
* [Troubleshooting Guide](TROUBLESHOOTING.md) — Remediation guide for common deployment challenges.
* [Architectural Limitations and Roadmap](LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md) — Comprehensive assessment of platform constraints and enhancements.
* [DevOps Technical Viva Q&A Guide](VIVA_QA.md) — Examination preparation guide and operational explanations.
* [Product Backlog Status](backlog.md) — Backlog items and MVP status.
* [Ansible Playbook Reference](../ansible/README.md) — Instructions and setup for containerized Ansible orchestration.

