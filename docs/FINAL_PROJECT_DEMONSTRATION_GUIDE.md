# Final Project Demonstration & Viva Defense Guide

A step-by-step practical manual for demonstrating all 15 tasks of the **Automated Digital Asset Approval Platform (DAAP)** during academic evaluation and technical viva examination.

---

## Before the Demonstration

### 1. Services & Ports Checklist
Ensure all 5 required services are running on their assigned host ports before starting the evaluation:

| Service | Host Port | Expected URL | Verification Command |
|---|---|---|---|
| **Jenkins CI/CD** | `8080` | `http://localhost:8080/job/digital-asset-approval-platform-pipeline/` | `Test-NetConnection -Port 8080 -ComputerName localhost` |
| **Apache Tomcat 10.1** | `8081` | `http://localhost:8081/digital-asset-approval-platform` | `Invoke-WebRequest -Uri http://localhost:8081/digital-asset-approval-platform/api/health` |
| **Docker Application** | `8082` | `http://localhost:8082/` | `Invoke-WebRequest -Uri http://localhost:8082/api/health` |
| **Ansible Target Node** | `8083` | `http://localhost:8083/` | `Invoke-WebRequest -Uri http://localhost:8083/api/health` |
| **Local Docker Registry**| `5000` | `http://localhost:5000/v2/_catalog` | `Invoke-WebRequest -Uri http://localhost:5000/v2/_catalog` |

### 2. Browser Tabs to Open in Advance
1. **Tab 1 (Jenkins Pipeline)**: `http://localhost:8080/job/digital-asset-approval-platform-pipeline/`
2. **Tab 2 (Tomcat Staging UI)**: `http://localhost:8081/digital-asset-approval-platform`
3. **Tab 3 (Docker Release UI)**: `http://localhost:8082/`
4. **Tab 4 (Ansible Target UI)**: `http://localhost:8083/`
5. **Tab 5 (Docker Registry API)**: `http://localhost:5000/v2/_catalog`
6. **Tab 6 (GitHub Repository)**: `https://github.com/Prasad0224/DevOps-Project/tree/develop`

### 3. Terminal & Editor Windows to Keep Open
* **VS Code / IDE**: Open to `DevOps-Project` workspace displaying `Jenkinsfile`, `ansible/site.yml`, and `README.md`.
* **PowerShell Terminal 1**: Working directory `C:\Users\prash\Desktop\DevOps\DevOps-Project`.

---

## Recommended Demonstration Order

To demonstrate the full project smoothly without restarting servers:
1. **Part A: Foundation & Application (Tasks 1–6)**: Show GitHub repository, user stories in `backlog.md`, Spring Boot code, and live UI workflows on Tomcat (`port 8081`).
2. **Part B: Continuous Integration & Selenium Gate (Tasks 7–10)**: Open Jenkins, show SCM polling, Maven compilation, test reports, and explain the Build #5 quality gate versus Build #6 promotion.
3. **Part C: Dockerization & Registry (Tasks 11–12)**: Show `Dockerfile`, local registry catalog on `port 5000`, running Docker container on `port 8082`, and Jenkins Docker CD stages.
4. **Part D: Ansible Configuration Management & Rollback (Tasks 13–14)**: Show `ansible/inventory.ini`, `site.yml`, idempotency proof (`changed=0`), and rollback mechanism restoring release 1.0.9.
5. **Part E: Complete 14-Stage Pipeline & Documentation (Task 15)**: Show Jenkins Build #10/11 overview, project documentation suite, and answer viva questions.

---

## 15-Minute Quick Demo Path

If time is limited, execute this rapid 4-step walk-through:
1. **Step 1 (2 mins) — Live Applications**: Show browser tabs on `8081` (Tomcat), `8082` (Docker), and `8083` (Ansible Target). Demonstrate that all three return HTTP 200 and host the identical functional UI.
2. **Step 2 (4 mins) — Full Jenkins Pipeline**: Open Jenkins Job. Show the 14-stage declarative pipeline (Build #10/11) spanning Checkout, Build, Tests, Package, Tomcat deploy, Selenium E2E, Docker build/tag/push/deploy, Production promotion, and Ansible provisioning.
3. **Step 3 (4 mins) — Quality Gate & Failure Blocking**: Open Build #5 in Jenkins. Show that when Selenium E2E tests failed, downstream Docker and Production stages were aborted. Then show Build #6 where all stages succeeded.
4. **Step 4 (5 mins) — Ansible Idempotency & Rollback**: In PowerShell, show `ansible/site.yml` idempotency log (`changed=0`) and explain atomic symlink rollback (`rollback.yml` restoring version 1.0.9).

---

## Full Task-by-Task Demonstration

---

# Task 1 — Problem Definition and Scope

## What This Task Proves
Proves the clear definition of an organizational digital asset approval bottleneck, distinct user roles (Requester vs Reviewer), strict file constraints (<=10MB; jpg, png, pdf, docx), and defined MVP boundaries.

## What I Need to Show the Professor
* File: `README.md` (Section 1) and `docs/backlog.md` (Section 1).
* Live Web UI on `http://localhost:8081/digital-asset-approval-platform`.

## Step-by-Step Demonstration
1. Open `docs/backlog.md` in the IDE.
2. Point out Section 1: Problem Statement, Pain Points, Stakeholder list, Constraints, and MVP Scope.
3. Open browser to `http://localhost:8081/digital-asset-approval-platform` and highlight the form fields and upload constraints.

## Exact Commands
```powershell
Get-Content docs/backlog.md -TotalCount 45
```

## Expected Result
Clearly defined problem statement, user roles (`requesterId`, `reviewerId`), file constraints, and measurable success criteria displayed.

## Evidence to Show
* `README.md` Section 1.
* `docs/backlog.md` Section 1.

## What to Explain
"Task 1 establishes the engineering scope. We replace disorganized email approvals with a centralized governance portal enforcing strict file constraints (<=10MB; jpg, png, pdf, docx) and role segregation between Requesters and Reviewers."

## Quick Verification Checklist
- [x] Problem statement and real-time need documented
- [x] Stakeholder and user roles defined
- [x] File constraints and MVP boundaries established

---

# Task 2 — Agile Planning and DevOps Workflow

## What This Task Proves
Demonstrates structured Agile Scrum planning with 6 detailed user stories (US-01 to US-06), explicit acceptance criteria, a 15-week milestone roadmap, a Definition of Done (DoD), and an end-to-end DevOps lifecycle flow.

## What I Need to Show the Professor
* File: `docs/backlog.md` (Sections 2 through 6).

## Step-by-Step Demonstration
1. Open `docs/backlog.md`.
2. Scroll to Section 2 (Product Backlog Table) and show delivery traceability.
3. Show Section 3 for User Stories US-01 through US-06 with Acceptance Criteria.
4. Show Section 4 for the 15-week sprint plan and Section 5 for the 7-point Definition of Done.

## Exact Commands
```powershell
Get-Content docs/backlog.md | Select-String "US-0|Sprint|Definition of Done"
```

## Expected Result
Complete alignment between agile backlog items, user stories, acceptance criteria, and delivered commits.

## Evidence to Show
* `docs/backlog.md` Sections 2–6.

## What to Explain
"We structured development into 10 sprints across 15 weeks. Each feature corresponds to a specific user story with strict acceptance criteria, governed by a rigorous 7-point Definition of Done."

## Quick Verification Checklist
- [x] User stories US-01 to US-06 documented with acceptance criteria
- [x] Product backlog status tracked to release v1.0.0-mvp
- [x] 15-week plan, DoD, and DevOps workflow diagram present

---

# Task 3 — Requirements, Architecture and Technology Setup

## What This Task Proves
Validates the Java 17 LTS and Spring Boot 3.1.2 software stack, Maven build configuration, REST API endpoints, responsive web application UI, and Apache Tomcat 10.1.x compatibility.

## What I Need to Show the Professor
* File: `pom.xml` (Java 17, Spring Boot 3.1.2, `<packaging>war</packaging>`).
* Live Web UI: `http://localhost:8081/digital-asset-approval-platform`.
* Live Health Probe: `http://localhost:8081/digital-asset-approval-platform/api/health`.

## Step-by-Step Demonstration
1. Open `pom.xml` and show `<java.version>17</java.version>` and `spring-boot-starter-tomcat` (provided scope).
2. Open `src/main/java/com/platform/DigitalAssetApprovalApplication.java` showing `SpringBootServletInitializer`.
3. Open browser to `http://localhost:8081/digital-asset-approval-platform/api/health` to demonstrate live response.

## Exact Commands
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/digital-asset-approval-platform/api/health" -UseBasicParsing
```

## Expected Result
HTTP 200 response with `"Application is running."`.

## Evidence to Show
* `pom.xml` lines 15–35.
* `docs/screenshots/app_ui_live.png`.

## What to Explain
"We chose Java 17 LTS and Spring Boot 3.1.2 packaged as a WAR archive. Extending `SpringBootServletInitializer` enables seamless dual-mode execution—both as a standalone Spring Boot application and inside Apache Tomcat 10.1.60."

## Quick Verification Checklist
- [x] Java 17 LTS and Spring Boot 3.1.2 configured
- [x] REST API endpoints (`/api/health`, `/api/requests`) active
- [x] Tomcat servlet container deployment verified on port 8081

---

# Task 4 — Git and GitHub Repository Initialization

## What This Task Proves
Proves professional version control management with `main` and `develop` branch topology, standardized linear Git history, meaningful commit conventions, and GitHub issue templates.

## What I Need to Show the Professor
* GitHub Repository: `https://github.com/Prasad0224/DevOps-Project`.
* Branch list and Issue Templates directory `.github/ISSUE_TEMPLATE/`.

## Step-by-Step Demonstration
1. Open PowerShell terminal.
2. Run `git branch -a` and `git log --oneline -10`.
3. Open `.github/ISSUE_TEMPLATE/bug_report.md` and `feature_request.md` in IDE.

## Exact Commands
```powershell
git branch -a
git log --oneline --decorate -10
```

## Expected Result
Branch list displays `develop` and `main`, commit messages follow conventional formats, and issue templates are structured.

## Evidence to Show
* Git log output in terminal.
* `.github/ISSUE_TEMPLATE/bug_report.md`.

## What to Explain
"Our Git repository follows Git Flow principles with protected `main` and `develop` branches. All changes are tracked with conventional commits and standardized GitHub issue templates."

## Quick Verification Checklist
- [x] `develop` and `main` branches present
- [x] Linear, meaningful commit history
- [x] GitHub issue templates configured

---

# Task 5 — Feature Development with Branching

## What This Task Proves
Demonstrates the isolated development of Feature 1 (Asset Submission Workflow and Validation) on dedicated branch `feature/asset-submission-workflow`, covered by unit tests and merged via Pull Request #1.

## What I Need to Show the Professor
* Git Merge Commit `b971736` (Merge pull request #1).
* Source code: `src/main/java/com/platform/service/AssetRequestService.java`.
* Test file: `src/test/java/com/platform/service/AssetRequestServiceTest.java`.

## Step-by-Step Demonstration
1. Open PowerShell and search git log for PR #1 merge.
2. Open `AssetRequestService.java` showing `validateSubmission()` rules.
3. Open `http://localhost:8081/digital-asset-approval-platform` and submit an asset live (Title: "Q1 Campaign", Requester: "MarketingUser", File: sample.png).

## Exact Commands
```powershell
git log --grep="pull request #1" --oneline
```

## Expected Result
Asset is accepted, assigned a UUID, and immediately appears in the queue with status `PENDING`.

## Evidence to Show
* Git commit `b971736`.
* Live UI submission feedback message.

## What to Explain
"Feature 1 was developed on branch `feature/asset-submission-workflow` and integrated via Pull Request #1. The service layer strictly validates title length, file size (<=10MB), and permitted extensions before generating a UUID."

## Quick Verification Checklist
- [x] Feature branch workflow demonstrated via PR #1
- [x] Submission and validation logic functional
- [x] Unit test suite covering edge cases

---

# Task 6 — MVP Completion and Git Collaboration

## What This Task Proves
Demonstrates the completion of Feature 2 (Reviewer Approval/Rejection Workflow, Audit Trail, and Notifications), intentional merge conflict creation and resolution, integration via Pull Request #2, and formal release tagging with `v1.0.0-mvp`.

## What I Need to Show the Professor
* Git Commits: `9042e02` (Merge conflict resolution), `ee4a1b4` (PR #2 merge), and `29f97a7` (Tag `v1.0.0-mvp`).
* Unit test execution: 7/7 tests passing.
* Live Reviewer workflow in the browser.

## Step-by-Step Demonstration
1. In browser at `http://localhost:8081/digital-asset-approval-platform`, locate the pending asset in the queue.
2. Click **Review**, enter Reviewer ID "ReviewerLead", enter comment "Approved for publication", and click **Submit Review**.
3. Point out status badge transition to `APPROVED` and audit log creation.
4. Run `git tag` in terminal to show `v1.0.0-mvp`.

## Exact Commands
```powershell
git tag -n
mvn test
```

## Expected Result
Status updates to `APPROVED`, all 7 unit tests pass with zero failures, and Git tag `v1.0.0-mvp` is verified.

## Evidence to Show
* Terminal output showing `Tests run: 7, Failures: 0, Errors: 0`.
* Tag `v1.0.0-mvp` in Git history.

## What to Explain
"Task 6 completed our MVP. We introduced reviewer workflows (`APPROVED`, `REJECTED`, `CHANGES_REQUESTED`), immutable audit logging, demonstrated Git conflict resolution during PR #2 merge, and tagged the release baseline as `v1.0.0-mvp`."

## Quick Verification Checklist
- [x] Reviewer workflows and audit logging verified
- [x] Git conflict resolution documented in commit history
- [x] Release tagged as `v1.0.0-mvp`
- [x] 7/7 unit tests passing

---

# Task 7 — Jenkins Installation and Continuous Integration Job

## What This Task Proves
Validates Jenkins CI automation on port `8080`, SCM polling trigger (`pollSCM`), automated Maven build and test execution, JUnit XML test reporting, and WAR artifact archiving with fingerprinting.

## What I Need to Show the Professor
* Jenkins Web UI: `http://localhost:8080/job/digital-asset-approval-platform-pipeline/`.
* Job configuration: SCM polling schedule `H/5 * * * *`.
* Test Result Trend chart and Archived Artifact `digital-asset-approval-platform.war`.

## Step-by-Step Demonstration
1. Open browser to `http://localhost:8080/job/digital-asset-approval-platform-pipeline/`.
2. Click on the latest build (Build #11).
3. Show **Artifacts** section: `digital-asset-approval-platform.war`.
4. Click **Test Result** and show 7 unit tests passed.

## Exact Commands
```powershell
Get-Content C:\Users\prash\.jenkins\jobs\digital-asset-approval-platform-pipeline\builds\11\polling.log
```

## Expected Result
Jenkins job displays automated SCM polling triggers, green test reports, and archived WAR package.

## Evidence to Show
* `docs/screenshots/jenkins_pipeline_overview.png`.
* `docs/screenshots/jenkins_build_6_success.png`.

## What to Explain
"Task 7 establishes continuous integration. Jenkins monitors the GitHub repository every 5 minutes via SCM polling. Whenever code is pushed, Jenkins automatically triggers a build, executes 7 unit tests, publishes JUnit reports, and archives the WAR artifact."

## Quick Verification Checklist
- [x] Jenkins operational on port 8080
- [x] SCM polling configured (`H/5 * * * *`)
- [x] Automated unit test reporting and artifact archiving verified

---

# Task 8 — Pipeline as Code and Server Deployment

## What This Task Proves
Demonstrates a declarative `Jenkinsfile` at repository root defining environment parameters (`DEPLOY_ENV`, `TOMCAT_PORT`), sequential stage execution, and automated deployment of the WAR package to Apache Tomcat 10.1.60 on port `8081`.

## What I Need to Show the Professor
* File: `Jenkinsfile` in repository root.
* Jenkins Stage View showing Stages 1–6.
* Staged application running live at `http://localhost:8081/digital-asset-approval-platform`.

## Step-by-Step Demonstration
1. Open `Jenkinsfile` in IDE and point out `pipeline { ... }`, `parameters { ... }`, and `stage('Test/Staging Deployment')`.
2. Show Tomcat server port `8081` parameterization.
3. Open browser to `http://localhost:8081/digital-asset-approval-platform` to prove Tomcat is serving the deployment.

## Exact Commands
```powershell
Get-Content Jenkinsfile | Select-String "TOMCAT_PORT|Test/Staging Deployment"
```

## Expected Result
WAR is copied to Tomcat `webapps/`, context initializes, and readiness loop confirms HTTP 200 before proceeding.

## Evidence to Show
* `Jenkinsfile` lines 1–125.
* `docs/screenshots/jenkins_build_6_stages.png`.

## What to Explain
"Pipeline as Code eliminates manual deployment scripts. Our declarative `Jenkinsfile` defines environment variables, compiles the code, executes unit tests, and deploys the WAR directly to Apache Tomcat on port 8081 with automated health polling."

## Quick Verification Checklist
- [x] Declarative `Jenkinsfile` at repository root
- [x] Parameterized environment settings (`TOMCAT_PORT: 8081`)
- [x] Successful Tomcat staging deployment verified

---

# Task 9 — Selenium Test Design and Local Execution

## What This Task Proves
Demonstrates automated browser-level regression testing using Selenium WebDriver 4.16.1 in headless Google Chrome, executing 5 end-to-end user journeys against the Tomcat deployment with automated screenshot capture on assertion failure.

## What I Need to Show the Professor
* Source code: `src/test/java/com/platform/selenium/AssetApprovalSeleniumTest.java`.
* 5 E2E test methods and JUnit 5 `TestWatcher` screenshot callback.
* Maven command executing Selenium profile.

## Step-by-Step Demonstration
1. Open `AssetApprovalSeleniumTest.java` in IDE.
2. Point out the 5 test cases (`test1_AssetSubmissionSuccess`, `test2_AssetSubmissionValidation_UnsupportedExtension`, etc.).
3. Point out `failed()` callback saving screenshots to `target/selenium-screenshots/`.

## Exact Commands
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"; $env:PATH = "C:\Program Files\Java\jdk-17\bin;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.3.2\plugins\maven\lib\maven3\bin;" + $env:PATH; mvn test -Pselenium -Dapp.baseUrl=http://localhost:8081/digital-asset-approval-platform -Dselenium.headless=true
```

## Expected Result
```
[INFO] Running com.platform.selenium.AssetApprovalSeleniumTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Evidence to Show
* `src/test/java/com/platform/selenium/AssetApprovalSeleniumTest.java`.
* `target/surefire-reports/TEST-com.platform.selenium.AssetApprovalSeleniumTest.xml`.

## What to Explain
"Task 9 delivers automated browser validation. We created 5 headless Chrome E2E tests using explicit waits and WebDriverManager. If any UI assertion fails, a full-page PNG screenshot is automatically saved for debugging."

## Quick Verification Checklist
- [x] 5 critical browser user journeys implemented
- [x] Headless Chrome configuration with explicit waits
- [x] Automated failure screenshot mechanism verified
- [x] 5/5 tests passing cleanly

---

# Task 10 — Continuous Testing in Jenkins & Production Quality Gate

## What This Task Proves
Demonstrates Selenium WebDriver integrated as a strict Quality Gate in the Jenkins pipeline. If any Selenium test fails, downstream deployment and production promotion stages are aborted (demonstrated in Build #5). After defect correction, Build #6 passed all tests and promoted the artifact.

## What I Need to Show the Professor
* Jenkins Build #5: Showing Stage 7 (`Selenium Tests`) **FAILED** and Stage 8 (`Production Deployment`) **SKIPPED**.
* Jenkins Build #6: Showing all 8 stages **SUCCESS** and WAR promoted to `deployments/production/`.
* Archived failure screenshot in Build #5.

## Step-by-Step Demonstration
1. Open Jenkins to Build #5 (`http://localhost:8080/job/digital-asset-approval-platform-pipeline/5/`).
2. Show that Stage 7 is red (FAILED) and Production Deployment is gray (SKIPPED).
3. Open Build #6 (`http://localhost:8080/job/digital-asset-approval-platform-pipeline/6/`) showing all stages green.
4. Open directory `deployments/production/` in IDE showing promoted WAR.

## Exact Commands
```powershell
Get-ChildItem deployments/production/
```

## Expected Result
Build #5 halted release; Build #6 verified tests and promoted `digital-asset-approval-platform.war`.

## Evidence to Show
* `docs/screenshots/jenkins_build_5_failure_gate.png`.
* `docs/screenshots/jenkins_build_6_stages.png`.

## What to Explain
"Task 10 proves our automated quality gate. In Build #5, a deliberate UI failure caused Selenium to fail; the pipeline immediately caught the failure, aborted downstream execution, and skipped production promotion. In Build #6, after fixing the issue, the pipeline passed and promoted the release."

## Quick Verification Checklist
- [x] Selenium E2E integrated into declarative pipeline
- [x] Quality Gate failure halts release (Build #5 verified)
- [x] Defect correction and production promotion verified (Build #6)

---

# Task 11 — Docker Image and Container Lifecycle

## What This Task Proves
Demonstrates containerization using an optimized `Dockerfile` based on `eclipse-temurin:17-jre`, container build/tagging, running container on host port `8082`, container lifecycle management (`inspect`, `logs`, `stop`, `start`, `rm`), and local Docker registry setup on port `5000`.

## What I Need to Show the Professor
* File: `Dockerfile` and `.dockerignore`.
* Running container: `digital-asset-approval-platform` on port `8082`.
* Running registry: `daap-registry` on port `5000`.
* Live UI on `http://localhost:8082/` and health check `/api/health`.

## Step-by-Step Demonstration
1. Open `Dockerfile` in IDE.
2. In terminal, run `docker ps` to display active containers.
3. Query container health endpoint on port `8082`.
4. Query registry catalog on port `5000`.

## Exact Commands
```powershell
docker ps --filter "name=digital-asset-approval-platform"
docker ps --filter "name=daap-registry"
Invoke-WebRequest -Uri "http://localhost:8082/api/health" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:5000/v2/_catalog" -UseBasicParsing
```

## Expected Result
Containers are healthy; port 8082 returns `"Application is running."`; port 5000 returns repositories catalog.

## Evidence to Show
* `docs/screenshots/docker_app_ui.png`.
* `docs/screenshots/docker_health_check.png`.

## What to Explain
"Task 11 containerizes our application using Eclipse Temurin Java 17 JRE. We established a local Docker registry on port 5000 and mapped container port 8080 to host port 8082, verifying complete container lifecycle operations."

## Quick Verification Checklist
- [x] `Dockerfile` and `.dockerignore` properly configured
- [x] Local Docker registry operational on port 5000
- [x] Docker application container running and healthy on port 8082

---

# Task 12 — Jenkins-Docker Continuous Deployment

## What This Task Proves
Demonstrates continuous deployment where Jenkins automatically builds the Docker image with tag `1.0.${BUILD_NUMBER}`, tags for the local registry, pushes to `localhost:5000`, deploys a fresh container on port `8082`, and polls `/api/health`—strictly after Selenium E2E tests pass.

## What I Need to Show the Professor
* Jenkins pipeline stage view showing:
  - `Docker Build`
  - `Docker Tag`
  - `Docker Push`
  - `Docker Deploy`
* Registry catalog listing versioned tags.

## Step-by-Step Demonstration
1. Open Jenkins Job overview (`http://localhost:8080/job/digital-asset-approval-platform-pipeline/`).
2. Show Stages 8 through 11 in Build #9, #10, and #11.
3. Open `http://localhost:5000/v2/digital-asset-approval-platform/tags/list` in browser.

## Exact Commands
```powershell
Invoke-RestMethod -Uri "http://localhost:5000/v2/digital-asset-approval-platform/tags/list"
```

## Expected Result
```json
{
  "name": "digital-asset-approval-platform",
  "tags": ["1.0.0", "1.0.9", "1.0.10", "1.0.11", "latest"]
}
```

## Evidence to Show
* `docs/screenshots/jenkins_build_9_stages.png`.
* `docs/screenshots/jenkins_build_9_console.png`.

## What to Explain
"Task 12 automates container delivery. Following successful Selenium testing, Jenkins builds a versioned Docker image, pushes it to our local Docker registry, stops and removes the old container, and launches a fresh container on port 8082, confirming health automatically."

## Quick Verification Checklist
- [x] Jenkins pipeline extended with 4 Docker CD stages
- [x] Versioned image tagging (`1.0.${BUILD_NUMBER}`) and registry push
- [x] Automated container redeployment with health check validation

---

# Task 13 — Configuration Management with Ansible

## What This Task Proves
Demonstrates agentless configuration management using Ansible to provision an isolated Ubuntu target node (`daap-target` on port `8083`), installing Java 17, curl, and supervisor, creating system user `daap`, managing `/opt/daap/` directory structure, deploying application configuration, and configuring process supervisor.

## What I Need to Show the Professor
* Files in `ansible/`: `inventory.ini`, `site.yml`, `rollback.yml`.
* First execution log showing initial configuration.

## Step-by-Step Demonstration
1. Open `ansible/site.yml` in IDE.
2. Show tasks: package installation, user creation, directory creation, config deployment, and supervisor service.
3. Open log `docs/evidence/ansible_first_run.txt`.

## Exact Commands
```powershell
Get-Content docs/evidence/ansible_first_run.txt -Tail 20
```

## Expected Result
`PLAY RECAP: daap-target : ok=17 changed=11 unreachable=0 failed=0`

## Evidence to Show
* `docs/evidence/ansible_first_run.txt`.
* `docs/screenshots/ansible_first_run.png`.

## What to Explain
"Task 13 implements Ansible configuration management. The playbook installs Java 17 and supervisor, creates a dedicated non-root `daap` user, creates versioned release directories in `/opt/daap/`, and manages the Spring Boot process under supervisor on port 8083."

## Quick Verification Checklist
- [x] Ansible inventory and playbook configured
- [x] System packages, non-root user, and directories managed
- [x] First run executed with `ok=17 changed=11 failed=0`

---

# Task 14 — Automated Provisioning, Idempotency & Rollback

## What This Task Proves
Proves two critical configuration management principles:
1. **Idempotency**: Running `site.yml` a second time against the target produces `changed=0`.
2. **Automated Rollback**: Executing `rollback.yml` restores the previous stable release (`1.0.9`) after an invalid release, atomically repointing symlink `/opt/daap/current` and restoring HTTP 200 health.

## What I Need to Show the Professor
* Idempotency evidence: `docs/evidence/ansible_idempotency_run.txt` (`changed=0`).
* Rollback evidence: `docs/evidence/ansible_rollback_run.txt` (`changed=2`, restored version 1.0.9).
* Live Target Node running on `http://localhost:8083/`.

## Step-by-Step Demonstration
1. Open `docs/evidence/ansible_idempotency_run.txt` and point out `ok=16 changed=0`.
2. Open `docs/evidence/ansible_rollback_run.txt` and show atomic symlink repointing.
3. Open browser to `http://localhost:8083/` and verify the live application is responding.

## Exact Commands
```powershell
Get-Content docs/evidence/ansible_idempotency_run.txt -Tail 5
Get-Content docs/evidence/ansible_rollback_run.txt -Tail 8
Invoke-WebRequest -Uri "http://localhost:8083/api/health" -UseBasicParsing
```

## Expected Result
* Idempotency run: `changed=0`.
* Rollback run: `ok=8 changed=2 failed=0`.
* Health check: `HTTP 200 Application is running.`.

## Evidence to Show
* `docs/screenshots/ansible_idempotency.png`.
* `docs/screenshots/ansible_rollback.png`.
* `docs/screenshots/ansible_target_app.png`.

## What to Explain
"Task 14 verifies reliability. Running the Ansible playbook a second time resulted in `changed=0`, proving true idempotency. In addition, when we simulated a defective release, `rollback.yml` atomically restored the active symlink to previous release 1.0.9 within seconds, restoring service availability immediately."

## Quick Verification Checklist
- [x] Idempotency validated (`ok=16 changed=0`)
- [x] Automated rollback demonstrated and verified (`rollback.yml`)
- [x] Target node healthy on port 8083

---

# Task 15 — Final End-to-End Release, Documentation & Viva

## What This Task Proves
Demonstrates the full 14-stage automated pipeline spanning Git push to multi-target release, comprehensive project documentation, clean repository structure, and complete viva examination readiness.

## What I Need to Show the Professor
* Jenkins Full Stage View (Build #10/11) showing all 14 stages green.
* Project documentation suite in `docs/`:
  - `DEVOPS_DOCUMENTATION.md`
  - `TROUBLESHOOTING.md`
  - `LIMITATIONS_AND_FUTURE_ENHANCEMENTS.md`
  - `VIVA_QA.md`
* Clean repository structure (`git status`).

## Step-by-Step Demonstration
1. Open Jenkins Job overview showing the complete 14-stage pipeline.
2. In IDE, browse the `docs/` folder showing the documentation suite and evidence screenshots.
3. Open PowerShell and run `git status` showing clean working tree.

## Exact Commands
```powershell
git status
Get-ChildItem docs/
```

## Expected Result
All 14 stages pass; working tree is clean; comprehensive documentation suite is present.

## Evidence to Show
* `docs/screenshots/jenkins_final_pipeline.png`.
* `docs/DEVOPS_DOCUMENTATION.md`.
* `docs/VIVA_QA.md`.

## What to Explain
"Task 15 consolidates our entire DevOps project into a production-ready, auditable platform. From code commit, Jenkins compiles, tests, stages on Tomcat, validates via Selenium, builds and pushes Docker images, promotes to production, provisions the target host via Ansible, and validates health across all tiers."

## Quick Verification Checklist
- [x] Full 14-stage declarative pipeline operational
- [x] Comprehensive documentation suite completed
- [x] All 15 PNG screenshots and 4 TXT logs archived
- [x] Repository clean and submission ready
