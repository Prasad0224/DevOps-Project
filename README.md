# Automated Digital Asset Approval Platform

A production-ready, auditable web application and continuous delivery pipeline for submitting digital assets for role-based approval with authentication, real-time status tracking, reviewer decision workflows, feedback visibility, resubmission, notifications, containerization, and configuration management.

---

## 1. Project Purpose & Key Features
The **Automated Digital Asset Approval Platform (DAAP)** centralizes and accelerates the review and approval lifecycle for organizational media assets (images, documents, PDFs):
* **Role-Based Authentication**: Session-based login with two roles — `USER` and `ADMIN`. Demo accounts: `user/user123` and `admin/admin123`.
* **Separate Dashboards**: Dedicated USER and ADMIN dashboards with role enforcement on both frontend and backend.
* **Asset Submission**: Real multipart file upload with client-side and server-side validation (≤10 MB; jpg, jpeg, png, pdf, docx). Files stored with UUID-prefixed names to prevent collisions.
* **Reviewer Workflow**: Admin portal supporting `APPROVED`, `REJECTED`, and `CHANGES_REQUESTED` actions with mandatory comment/feedback.
* **Feedback Visibility**: Admin comments stored and displayed to users in dashboard and request history.
* **Resubmission**: Users upload revised assets when `CHANGES_REQUESTED`; resets status to `PENDING`.
* **In-Memory Notifications**: Real notification events shown in UI for USER (status updates) and ADMIN (new submissions).
* **Audit Trail & Review History**: Chronological timeline of all events per request, visible to both roles.
* **File Preview & Download**: Admins can preview images/PDFs inline and download uploaded files.
* **Authorization Enforcement**: Backend `403`/`401` for unauthorized access; frontend hides admin controls from users.
* **Enterprise CI/CD**: End-to-end from Git commit through Jenkins, Tomcat staging, Selenium regression (13 tests), Docker packaging, registry, and Ansible provisioning.

---

## 2. Technology Stack

| Layer | Technology | Specification / Version |
|---|---|---|
| **Backend** | Java / Spring Boot | Java 17 LTS, Spring Boot 3.1.2 |
| **Packaging** | Apache Maven | Maven 3.9.x, Standard WAR (`spring-boot-starter-tomcat` provided) |
| **Frontend** | Single-Page Application | Responsive HTML5, Vanilla CSS3, JavaScript |
| **Servlet Container** | Apache Tomcat | Version 10.1.60 (Port 8081) |
| **CI/CD Orchestration**| Jenkins LTS | Version 2.568.3 (Port 8080) |
| **Automated Testing** | Selenium WebDriver | Version 4.16.1, JUnit 5, Headless Chrome |
| **Container Engine** | Docker Desktop | Engine 29.2.1, Temurin 17 JRE Base |
| **Container Registry**| Docker Registry v2 | Official `registry:2` (Port 5000) |
| **Container Release** | Docker Container | Port 8082 -> 8080 |
| **Config Management** | Ansible Core | Containerized Controller (`daap-ansible-img`) |
| **Target Host** | Ubuntu Linux | Ubuntu 22.04 LTS (`daap-target`, Port 8083) |
| **Process Manager** | Supervisor | Version 4.2.4 (User `daap`) |

---

## 3. High-Level Architecture

```
[Developer] ──► [GitHub (origin/develop)]
                       │
                       ▼ (SCM Polling H/5)
               [Jenkins CI/CD Pipeline (Port 8080)]
                       │
        ┌──────────────┼──────────────────────────────┐
        ▼              ▼                              ▼
  [Maven Build]  [Tomcat 10.1 (Port 8081)]    [Docker Engine]
  - Compile      - WAR Staged                 - Build & Tag (1.0.${BUILD_NUMBER})
  - 19 Unit Tests- 13 Selenium E2E Tests      - Push to Local Registry (Port 5000)
                 - Quality Gate Trigger       - Deploy App Container (Port 8082)
                                                      │
                                                      ▼
                                            [Ansible Provisioning]
                                            - Target Host: daap-target (Port 8083)
                                            - Idempotent State Management
                                            - Atomic Rollback via Supervisor
```

---

## 4. Repository Structure

```
DevOps-Project/
├── .github/
│   └── ISSUE_TEMPLATE/
│       ├── bug_report.md
│       └── feature_request.md
├── ansible/
│   ├── Dockerfile.controller
│   ├── Dockerfile.target
│   ├── inventory.ini
│   ├── requirements.yml
│   ├── rollback.yml
│   ├── site.yml
│   └── README.md
├── docs/
│   ├── evidence/
│   │   ├── ansible_first_run.txt
│   │   ├── ansible_health_check.txt
│   │   ├── ansible_idempotency_run.txt
│   │   └── ansible_rollback_run.txt
│   ├── screenshots/
│   │   ├── ansible_first_run.png
│   │   ├── ansible_health.png
│   │   ├── ansible_idempotency.png
│   │   ├── ansible_rollback.png
│   │   ├── ansible_target_app.png
│   │   ├── app_ui_live.png
│   │   ├── docker_app_ui.png
│   │   ├── docker_health_check.png
│   │   ├── jenkins_build_5_failure_gate.png
│   │   ├── jenkins_build_6_stages.png
│   │   ├── jenkins_build_6_success.png
│   │   ├── jenkins_build_9_console.png
│   │   ├── jenkins_build_9_stages.png
│   │   ├── jenkins_final_pipeline.png
│   │   └── jenkins_pipeline_overview.png
│   ├── backlog.md
│   ├── DEVOPS_DOCUMENTATION.md
│   ├── FINAL_PROJECT_DEMONSTRATION_GUIDE.md
│   ├── LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md
│   ├── TROUBLESHOOTING.md
│   └── VIVA_QA.md
├── src/
│   ├── main/
│   │   ├── java/com/platform/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── DigitalAssetApprovalApplication.java
│   │   └── resources/
│   │       ├── static/index.html
│   │       └── application.properties
│   └── test/
│       └── java/com/platform/
│           ├── selenium/AssetApprovalSeleniumTest.java
│           └── service/AssetRequestServiceTest.java
├── .dockerignore
├── .gitignore
├── Dockerfile
├── Jenkinsfile
├── pom.xml
└── README.md
```

---

## 5. Local Setup & Maven Commands

### Prerequisites
* **Java 17 LTS** (JDK)
* **Apache Maven 3.9+**
* **Google Chrome** (for Selenium E2E tests)
* **Docker Desktop** (with WSL2 enabled)

### Build & Unit Testing
```bash
# Clean compilation and execution of 19 unit tests
mvn clean test

# Package Tomcat-compatible WAR artifact
mvn package -DskipTests
```

---

## 6. Tomcat Deployment & Selenium Testing

### Deploying to Tomcat
1. Start Apache Tomcat 10.1.60 on port `8081` (`<TOMCAT_HOME>/bin/startup.bat`).
2. Copy `target/digital-asset-approval-platform.war` to `<TOMCAT_HOME>/webapps/`.
3. Application launches at: `http://localhost:8081/digital-asset-approval-platform`.

### Running Selenium E2E Tests
```bash
mvn test -Pselenium -Dapp.baseUrl=http://localhost:8081/digital-asset-approval-platform -Dselenium.headless=true
```
* Runs 13 automated browser test cases in headless Chrome.
* Automatically captures failure screenshots to `target/selenium-screenshots/` if assertions fail.

---

## 7. Docker Execution & Local Registry

### 1. Start Local Docker Registry
```bash
docker run -d --restart unless-stopped --name daap-registry -p 5000:5000 registry:2
```

### 2. Build and Tag Docker Image
```bash
docker build -t digital-asset-approval-platform:1.0.0 -t digital-asset-approval-platform:latest .
docker tag digital-asset-approval-platform:1.0.0 localhost:5000/digital-asset-approval-platform:1.0.0
docker tag digital-asset-approval-platform:latest localhost:5000/digital-asset-approval-platform:latest
```

### 3. Push to Local Registry
```bash
docker push localhost:5000/digital-asset-approval-platform:1.0.0
docker push localhost:5000/digital-asset-approval-platform:latest
```

### 4. Run Docker Application Container
```bash
docker run -d --name digital-asset-approval-platform -p 8082:8080 localhost:5000/digital-asset-approval-platform:1.0.0
```
* Application accessible at `http://localhost:8082/`.
* Health check: `http://localhost:8082/api/health`.

---

## 8. Jenkins CI/CD Pipeline

The declarative pipeline defined in [Jenkinsfile](Jenkinsfile) automates the complete 14-stage lifecycle:
1. **Checkout**: Source code retrieved from `develop` branch.
2. **Build**: Code compiled with `mvn compile`.
3. **Unit Tests**: 19 unit tests executed and JUnit report published.
4. **Package**: Production WAR generated via `mvn package -DskipTests`.
5. **Archive**: WAR artifact fingerprinted and archived.
6. **Staging Deployment**: Deployed to Apache Tomcat on port 8081; polls `/api/health`.
7. **Selenium Tests**: 13 headless Chrome E2E tests against Tomcat. **Quality Gate**: failure aborts stages 8–14.
8. **Docker Build**: Image built with build tag `1.0.${BUILD_NUMBER}`.
9. **Docker Tag**: Tagged for local registry (`localhost:5000`) and `latest`.
10. **Docker Push**: Pushed to local registry.
11. **Docker Deploy**: Deployed fresh container on port 8082; health check validated.
12. **Production Promotion**: Verified WAR promoted to `deployments/production/`.
13. **Ansible Provisioning**: Provisions target node (`daap-target`) on port 8083 via supervisor.
14. **Ansible Health Check**: Verified HTTP 200 on `http://localhost:8083/api/health`.

---

## 9. Ansible Configuration Management & Rollback

### Provisioning the Target Host
```bash
# Execute main deployment playbook
docker run --rm --network daap-net \
  -v "${PWD}/ansible:/ansible" \
  -v "${PWD}/target:/target" \
  daap-ansible-img -i inventory.ini site.yml
```
* **First Run**: `ok=17 changed=11 failed=0` (Initial configuration complete).
* **Second Run (Idempotency)**: `ok=16 changed=0 failed=0` (Zero redundant modifications).

### Automated Rollback Execution
```bash
# Rollback active release to previous stable release (e.g. 1.0.9)
docker run --rm --network daap-net \
  -v "${PWD}/ansible:/ansible" \
  -v "${PWD}/target:/target" \
  -e "rollback_version=1.0.9" \
  daap-ansible-img -i inventory.ini rollback.yml
```
* Result: `ok=8 changed=2 failed=0` (Atomically repointed symlink and verified HTTP 200).

---

## 10. Live Service Endpoints & Health Checks

| Service | Environment / Runtime | URL | Health Check URL | Status |
|---|---|---|---|---|
| **Jenkins Orchestrator** | Standalone CI Server | `http://localhost:8080/` | Job Status | **ACTIVE** |
| **Apache Tomcat 10.1** | Staging Application | `http://localhost:8081/digital-asset-approval-platform` | `/api/health` | **LIVE (200 OK)** |
| **Docker Application** | Container Release | `http://localhost:8082/` | `http://localhost:8082/api/health` | **LIVE (200 OK)** |
| **Ansible Target Node** | Ubuntu 22.04 Host | `http://localhost:8083/` | `http://localhost:8083/api/health` | **LIVE (200 OK)** |
| **Local Docker Registry**| Registry v2 | `http://localhost:5000/v2/_catalog` | `/tags/list` | **ACTIVE** |

---

## 11. Verified Test Results

* **Unit Tests**: **19/19 Passed** (`AssetRequestServiceTest.java`)
* **Selenium E2E Tests**: **13/13 Passed** (`AssetApprovalSeleniumTest.java`)
* **Total Automated Tests**: **32/32 Passed**
* **Jenkins Pipeline**: Build #6 (Tasks 7–10), Build #9 (Tasks 11–12), Build #10 (Tasks 13–15) all **SUCCESS**
* **Quality Gate**: Verified in Build #5 where Selenium failure aborted production release

---

## 12. Architectural Limitations

* Single-host academic execution environment.
* In-memory database persistence for asset records and audit logs.
* Ephemeral local Docker registry (`localhost:5000`) without external cloud replication.
* Plaintext HTTP transport across local service boundaries.

Detailed documentation: [LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md](docs/LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md).

---

## 13. Project Documentation Links

* [DevOps Documentation](docs/DEVOPS_DOCUMENTATION.md) — Comprehensive technical reference for Tasks 1–15.
* [Final Project Demonstration Guide](docs/FINAL_PROJECT_DEMONSTRATION_GUIDE.md) — Step-by-step practical manual for professor demonstration and viva.
* [Master Task 1–15 Verification Report](FINAL_TASK_1_15_VERIFICATION.md) — Independent compliance and audit verification matrix.
* [Troubleshooting Guide](docs/TROUBLESHOOTING.md) — Remediation guide for common deployment challenges.
* [Limitations & Future Enhancements](docs/LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md) — Architecture limits and production roadmap.
* [DevOps Viva Q&A](docs/VIVA_QA.md) — Viva examination guide and technical questions.
* [Product Backlog](docs/backlog.md) — Agile product backlog status.
* [Ansible Documentation](ansible/README.md) — Playbook architecture and execution guidelines.

---

## 14. License
MIT License
