# Automated Digital Asset Approval Platform

A modern, auditable web application and continuous delivery pipeline for submitting digital assets for approval with real-time status tracking, automated validation, and reviewer workflows.

---

## Project Overview & Verified Status

| Component / Task | Requirement | Verified Status | Evidence |
|---|---|---|---|
| **Tasks 1–6** | Architecture, Models, REST APIs, Validation & Unit Tests | **VERIFIED** | 7/7 unit tests passing, Spring Boot 3 + Java 17 |
| **Task 7** | Maven Build, WAR Packaging & Jenkins CI | **PASS** | Tomcat WAR packaging, Jenkins CI integration |
| **Task 8** | Declarative Jenkinsfile & Tomcat Deployment | **PASS** | 8-stage pipeline deployed to Apache Tomcat 10.1.60 |
| **Task 9** | Headless Selenium WebDriver E2E Test Suite | **PASS** | 5/5 E2E tests passing, screenshot on failure |
| **Task 10** | Selenium CI/CD Quality Gate & Promotion | **PASS** | Build #5 verified failure gate; Build #6 verified promotion |

* **Total Automated Tests**: 12/12 passed (7 Unit Tests + 5 Selenium E2E Tests).
* **Jenkins Pipeline Execution**: Build #6 **SUCCESS** (`digital-asset-approval-platform-pipeline`).
* **Failure Gate Verification**: Build #5 demonstrated that a Selenium failure aborts the pipeline and skips production promotion.
* **Apache Tomcat Runtime**: Version 10.1.60 running on HTTP port **8081**.
* **Live Application URL**: [http://localhost:8081/digital-asset-approval-platform](http://localhost:8081/digital-asset-approval-platform)
* **Health Check URL**: [http://localhost:8081/digital-asset-approval-platform/api/health](http://localhost:8081/digital-asset-approval-platform/api/health)

---

## Tech Stack
* **Backend**: Java 17, Spring Boot 3.1.2
* **Packaging**: Standard Web Archive (`.war`) via `spring-boot-starter-tomcat` (provided scope)
* **Frontend**: Responsive Single-Page Application (HTML5, Vanilla CSS3, JavaScript)
* **Web Server / Servlet Container**: Apache Tomcat 10.1.60 (Port 8081)
* **CI/CD Automation**: Jenkins 2.568.3 (Java 21, Port 8080)
* **End-to-End Testing**: Selenium WebDriver 4.16.1, WebDriverManager 5.6.3, Chrome Headless
* **Unit Testing**: JUnit 5, Mockito, Spring Boot Test

---

## Jenkins CI/CD Pipeline Architecture

The declarative [Jenkinsfile](Jenkinsfile) enforces sequential quality gates:

```
[1. Checkout]
      ↓
[2. Build] (mvn compile)
      ↓
[3. Unit Tests] (mvn test - 7 unit tests) ─── [JUnit Test Reports Recorded]
      ↓
[4. Package] (mvn package - WAR build)
      ↓
[5. Archive] (target/*.war Fingerprinted)
      ↓
[6. Test/Staging Deployment] (Deployed to Tomcat 10.1.60 @ port 8081)
      ↓
[7. Selenium Tests] ───┬─── [FAILED] ──→ Pipeline Aborted (Production Promotion Skipped)
                       └─── [PASSED] ──→ [8. Production Deployment]
                                                    ↓
                                      (Promoted to deployments/production/)
```

### Note on Production Promotion
This single-machine academic DevOps project does not operate an external production server. The pipeline stage labeled **Production Deployment** implements **Production Artifact Promotion**, copying the Selenium-verified WAR artifact to the release directory `deployments/production/` upon successful test completion.

---

## Local Development & Manual Run

### 1. Build and Run Unit Tests
```bash
mvn clean test
```

### 2. Package WAR
```bash
mvn package -DskipTests
```

### 3. Run Selenium Tests Against Tomcat
Ensure Tomcat is running on port 8081 and the application is deployed:
```bash
mvn test -Pselenium -Dapp.baseUrl=http://localhost:8081/digital-asset-approval-platform -Dselenium.headless=true
```

---

## Repository Documentation Index
* [FINAL_TASK_1_6_VERIFICATION.md](FINAL_TASK_1_6_VERIFICATION.md) — Detailed verification of Tasks 1 through 6.
* [FINAL_TASK_7_10_AUDIT.md](FINAL_TASK_7_10_AUDIT.md) — Final audit and verification matrix for Tasks 7 through 10.
* [TASK_7_IMPLEMENTATION.md](TASK_7_IMPLEMENTATION.md) — Task 7 implementation details.
* [TASK_8_IMPLEMENTATION.md](TASK_8_IMPLEMENTATION.md) — Task 8 implementation details.
* [TASK_9_IMPLEMENTATION.md](TASK_9_IMPLEMENTATION.md) — Task 9 implementation details.
* [TASK_10_IMPLEMENTATION.md](TASK_10_IMPLEMENTATION.md) — Task 10 implementation details.

---

## License
MIT License
