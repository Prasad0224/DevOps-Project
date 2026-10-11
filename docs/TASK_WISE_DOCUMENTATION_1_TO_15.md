# Automated Digital Asset Approval Platform
# FINAL TASK-WISE DOCUMENTATION (TASK 1 TO TASK 15)

## Project Information
**Project**: Automated Digital Asset Approval Platform (DAAP)
**Repository**: C:\Users\prash\Desktop\DevOps\DevOps-Project
**Branch**: develop
**GitHub**: https://github.com/Prasad0224/DevOps-Project.git

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Final System Architecture](#2-final-system-architecture)
3. [Final Business Workflow](#3-final-business-workflow)
4. [Complete DevOps Lifecycle](#4-complete-devops-lifecycle)
5. [TASK 1 - Problem Definition and Scope](#5-task-1---problem-definition-and-scope)
6. [TASK 2 - User Stories and Agile Planning](#6-task-2---user-stories-and-agile-planning)
7. [TASK 3 - Core Architecture Setup](#7-task-3---core-architecture-setup)
8. [TASK 4 - Git Foundation and Branching](#8-task-4---git-foundation-and-branching)
9. [TASK 5 - Asset Submission and Validation](#9-task-5---asset-submission-and-validation)
10. [TASK 6 - Review Workflows and MVP Completion](#10-task-6---review-workflows-and-mvp-completion)
11. [TASK 7 - Jenkins CI Automation](#11-task-7---jenkins-ci-automation)
12. [TASK 8 - Declarative Pipeline and Tomcat Staging](#12-task-8---declarative-pipeline-and-tomcat-staging)
13. [TASK 9 - Automated E2E Testing](#13-task-9---automated-e2e-testing)
14. [TASK 10 - Quality Gates and Continuous Testing](#14-task-10---quality-gates-and-continuous-testing)
15. [TASK 11 - Docker Containerization](#15-task-11---docker-containerization)
16. [TASK 12 - Local Registry and Docker CD](#16-task-12---local-registry-and-docker-cd)
17. [TASK 13 - Ansible Configuration Management](#17-task-13---ansible-configuration-management)
18. [TASK 14 - Idempotency and Rollback](#18-task-14---idempotency-and-rollback)
19. [TASK 15 - Final Pipeline and Documentation](#19-task-15---final-pipeline-and-documentation)
20. [Final Compliance Matrix](#20-final-compliance-matrix)
21. [Technology Matrix](#21-technology-matrix)
22. [Complete Test Results](#22-complete-test-results)
23. [Professor Demonstration Guide](#23-professor-demonstration-guide)
24. [Viva Questions and Answers](#24-viva-questions-and-answers)
25. [Final Conclusion](#25-final-conclusion)

---

## 1. Project Overview
The **Automated Digital Asset Approval Platform (DAAP)** is an enterprise-grade web application built to centralize, secure, and streamline the review and approval process for digital assets. It replaces error-prone email and chat workflows with a secure portal for Requesters (users) and Reviewers (admins). The platform integrates a full CI/CD pipeline, containerization, and configuration management for zero-downtime, fully automated deployments.

---

## 2. Final System Architecture
The system consists of a Spring Boot backend, a responsive HTML/JS/CSS frontend, and a completely automated DevOps deployment architecture. 

**Component Mapping:**
- **Jenkins Orchestrator**: Port 8080
- **Tomcat Staging Environment**: Port 8081
- **Docker Production Application**: Port 8082
- **Ansible Target Node**: Port 8083
- **Local Docker Registry**: Port 5000

---

## 3. Final Business Workflow
The complete authenticated business logic flow:

**USER** -> Login -> USER Dashboard -> Upload Real Asset -> PENDING -> **ADMIN** Dashboard -> Review Asset -> Preview / Download -> Approve / Reject / Request Changes -> Feedback + Notification + Audit History -> (If Changes Requested) -> USER sees feedback -> Resubmit -> PENDING -> ADMIN reviews -> APPROVED / REJECTED

This workflow is fully guarded by authentication and role-based authorization to ensure segregation of duties.

---

## 4. Complete DevOps Lifecycle
The implementation utilizes a 14-stage automated Jenkins pipeline:

```text
Developer
  ↓
GitHub (Push)
  ↓
Jenkins (SCM Poll)
  ↓
Maven Compile
  ↓
Unit Tests (19/19)
  ↓
WAR Package
  ↓
Tomcat Staging (Port 8081)
  ↓
Health Check
  ↓
Selenium E2E (13/13)
  ↓
Quality Gate (Aborts on Failure)
  ↓
Docker Build
  ↓
Docker Tag
  ↓
Docker Registry (Port 5000)
  ↓
Docker Deploy (Port 8082)
  ↓
Docker Health Check
  ↓
Ansible Provisioning (Target Port 8083)
  ↓
Ansible Idempotency
  ↓
Ansible Health Check
  ↓
Release / Rollback
```

---

## 5. TASK 1 - Problem Definition and Scope

**1. Objective / Requirement**
"Organizations require an automated, centralized, role-segregated portal to govern the submission, automated validation, and formal reviewer sign-off of digital assets prior to distribution and release. Define problem statement, target users, and constraints." (Source: backlog.md)

**2. Solution Implemented**
Defined clear roles for `USER` (Requester) and `ADMIN` (Reviewer). Defined strict boundaries, limiting uploads to max 10MB and specific extensions (.jpg, .jpeg, .png, .pdf, .docx).

**3. Technologies / Tools Used**
Markdown, Requirements Engineering

**4. Implementation Details**
Captured in `README.md` and `docs/backlog.md`. Roles and size limits were subsequently implemented in `AssetRequestService.java`.

**5. Execution / Working**
The project scope is constrained to exactly what is documented, preventing feature creep and providing a solid foundation for the MVP.

**6. Verification / Result**
Verified that constraints (10MB size limit, file type restrictions) operate actively within the running system and backend layers.

**7. Evidence / Screenshot**
*Screenshot not explicitly required for plain text requirements, but the implemented constraints can be observed in the live portal.*

**8. Viva Explanation**
"For Task 1, we defined the core problem: messy, unaccountable email approvals. We scoped the MVP to include strict roles, a 10MB limit, and core file types. We documented this directly in our backlog."

---

## 6. TASK 2 - User Stories and Agile Planning

**1. Objective / Requirement**
"Define Product Backlog Status and User Stories. Establish Agile Scrum & Kanban Plan." (Source: backlog.md)

**2. Solution Implemented**
Created six distinct User Stories (US-01 through US-06) covering submission, validation, review, dashboard tracking, audit, and notifications. Created a 15-week Agile roadmap.

**3. Technologies / Tools Used**
Agile, Scrum, Kanban, Markdown

**4. Implementation Details**
Captured comprehensively in `docs/backlog.md`. The Definition of Done (DoD) requires 100% test pass rate and automated staging deployment.

**5. Execution / Working**
The development lifecycle strictly followed this Agile plan. Every feature branch and Pull Request addressed a specific backlog item.

**6. Verification / Result**
All MVP backlog items are now marked as `Done` and verified in production.

**7. Evidence / Screenshot**
*Evidence consists of `docs/backlog.md` structure.*

**8. Viva Explanation**
"We mapped the requirements into 6 User Stories and laid out a 15-week sprint plan. The strict Definition of Done ensured quality at every step before merging into develop."

---

## 7. TASK 3 - Core Architecture Setup

**1. Objective / Requirement**
"Core Architecture & Technology Setup: Java 17, Spring Boot 3, Maven build, initial domain models, REST controllers, and responsive web UI." (Source: backlog.md)

**2. Solution Implemented**
Initialized a Spring Boot 3 application with Java 17. Defined the domain model (`AssetRequest.java`, `ReviewAction.java`). Built the core backend logic.

**3. Technologies / Tools Used**
Java 17, Spring Boot, Apache Maven, HTML/CSS/JS

**4. Implementation Details**
Created `pom.xml`, defined dependencies, set `<packaging>war</packaging>`, and initialized the REST controllers in `src/main/java/com/platform/`. 

**5. Execution / Working**
The application boots independently and serves a responsive single-page web interface.

**6. Verification / Result**
The web application compiles cleanly with `mvn clean install` and starts embedded or via an external Tomcat container.

**7. Evidence / Screenshot**
Figure 1: Application Web UI
*Evidence: This screenshot demonstrates the responsive web interface of the system.*

**8. Viva Explanation**
"For Task 3, we bootstrapped the foundation: Java 17, Spring Boot, and Maven. We chose WAR packaging to remain compatible with standard Tomcat staging environments."

---

## 8. TASK 4 - Git Foundation and Branching

**1. Objective / Requirement**
"Git Foundation & Branching Strategy: Git repository setup, issue templates, branch policies (develop, feature/*, release/*), commit rules." (Source: backlog.md)

**2. Solution Implemented**
Initialized Git, connected to the `Prasad0224/DevOps-Project.git` GitHub repository, created `.gitignore`, and established a `develop` branch for integration. Added GitHub issue templates.

**3. Technologies / Tools Used**
Git, GitHub

**4. Implementation Details**
Implemented local branching. Enforced the workflow where features are built on `feature/*` branches and merged into `develop`.

**5. Execution / Working**
Developers clone the repository, check out a feature branch, and submit code back via pull requests.

**6. Verification / Result**
Repository commit history confirms linear, structured feature branch merges into `develop`.

**7. Evidence / Screenshot**
Figure 2: GitHub Repository Branches
*Evidence: This screenshot demonstrates the Git workflow and repository branch structure.*

**8. Viva Explanation**
"We established a standard Gitflow model. No direct commits to main. Features were built on isolated branches and merged into develop, keeping our mainline stable."

---

## 9. TASK 5 - Asset Submission and Validation

**1. Objective / Requirement**
"Feature 1: Asset Submission & Validation. Asset submission form, file constraint validation engine, unit tests." (Source: backlog.md)

**2. Solution Implemented**
Created the multipart file upload controller. Implemented file size limit (10MB) and format validations. Wrote JUnit tests for the validation logic.

**3. Technologies / Tools Used**
Spring Web, Java, JUnit 5, HTML/JS

**4. Implementation Details**
The `AssetRequestController` and `AssetRequestService` handle the incoming data. UUIDs guarantee collision-free storage in the `uploads/` directory.

**5. Execution / Working**
A user accesses the dashboard, fills out the form, selects a `.pdf`, and submits. The system validates and saves the real file, marking it `PENDING`.

**6. Verification / Result**
Submission creates a valid UUID asset, saves the file to disk, and Unit Tests (19/19) verify the bounds checking.

**7. Evidence / Screenshot**
Figure 3: Asset Upload Validation
*Evidence: This screenshot demonstrates the platform actively validating submission rules.*

**8. Viva Explanation**
"This task implemented the actual file upload logic. We used UUIDs to prevent file name collisions and implemented strict backend validation to block oversized or malicious files."

---

## 10. TASK 6 - Review Workflows and MVP Completion

**1. Objective / Requirement**
"Feature 2: Review Workflows & MVP Completion. Reviewer actions (APPROVED/REJECTED/CHANGES_REQUESTED), audit trail, merge conflict resolution." (Source: backlog.md)

**2. Solution Implemented**
Implemented the ADMIN review capabilities, allowing admins to leave feedback, request changes, and transition asset states. Built the immutable audit logging.

**3. Technologies / Tools Used**
Java, Spring Boot, Git (Conflict Resolution)

**4. Implementation Details**
`ReviewAction` objects track the history. A feature branch (`feature/review-workflow`) was deliberately merged into `develop` to demonstrate git merge conflict handling.

**5. Execution / Working**
Admin clicks "Review", enters comments, and selects "Approve" or "Request Changes". The request state updates and is recorded in the audit trail. The user can view feedback and resubmit.

**6. Verification / Result**
The full end-to-end MVP functionality is complete and verified. The `v1.0.0-mvp` tag was generated.

**7. Evidence / Screenshot**
Figure 4: Audit History
*Evidence: This screenshot demonstrates the immutable audit logging of reviewer decisions.*

**8. Viva Explanation**
"We completed the core business logic. Admins can approve or reject with comments, and everything is permanently tracked in an audit log. We also simulated and resolved a Git merge conflict during this sprint."

---

## 11. TASK 7 - Jenkins CI Automation

**1. Objective / Requirement**
"Jenkins CI Automation: Declarative Jenkinsfile, Maven CI automation, SCM polling." (Source: backlog.md)

**2. Solution Implemented**
Configured Jenkins to poll the GitHub repository. Installed Maven on the Jenkins node to automate `mvn clean compile test package`.

**3. Technologies / Tools Used**
Jenkins, Maven, Git

**4. Implementation Details**
Created a pipeline job pointing to `develop`. Configured SCM polling (`H/5 * * * *`). Archiving artifacts and publishing JUnit reports.

**5. Execution / Working**
Every commit to the repository triggers Jenkins automatically within 5 minutes.

**6. Verification / Result**
Jenkins successfully builds the code, runs 19/19 unit tests, and packages the WAR file automatically.

**7. Evidence / Screenshot**
Figure 5: Jenkins Pipeline Job Overview
*Evidence: This screenshot demonstrates the automated Jenkins pipeline job configuration.*

**8. Viva Explanation**
"Task 7 was about eliminating manual builds. We connected Jenkins to GitHub with SCM polling, so every developer commit automatically triggers a clean Maven build and test run."

---

## 12. TASK 8 - Declarative Pipeline and Tomcat Staging

**1. Objective / Requirement**
"Declarative Jenkinsfile & Tomcat Staging: Deployment to Apache Tomcat 10.1.60." (Source: backlog.md)

**2. Solution Implemented**
Wrote a declarative `Jenkinsfile` at the repository root. Added a `Deploy to Tomcat` stage that copies the compiled `WAR` file to Tomcat's `webapps/` directory and checks health.

**3. Technologies / Tools Used**
Jenkins Pipeline (Groovy), Apache Tomcat 10.1

**4. Implementation Details**
The `Jenkinsfile` utilizes parameters (`TOMCAT_PORT=8081`). It uses `curl` or `Invoke-WebRequest` to verify the `/api/health` endpoint is alive post-deployment.

**5. Execution / Working**
Post-build, the `target/digital-asset-approval-platform.war` is pushed to Tomcat, bringing the staging environment online.

**6. Verification / Result**
The application runs stably on `http://localhost:8081`.

**7. Evidence / Screenshot**
Figure 6: Jenkins Staging Deployment Stage
*Evidence: This screenshot demonstrates the automated WAR deployment to the Tomcat staging environment.*

**8. Viva Explanation**
"We converted our CI steps into Pipeline-as-Code (`Jenkinsfile`). After building the WAR, Jenkins automatically copies it to Tomcat on port 8081, ensuring our staging environment is always up-to-date."

---

## 13. TASK 9 - Automated E2E Testing

**1. Objective / Requirement**
"Automated E2E Testing: Headless Chrome Selenium test suite (5 E2E tests), JUnit test reports." (Source: backlog.md)

**2. Solution Implemented**
Developed an extensive Selenium WebDriver suite in Java. The suite authenticates as USER and ADMIN, simulating real uploads, previewing, and approvals.

**3. Technologies / Tools Used**
Selenium WebDriver, ChromeDriver, JUnit 5

**4. Implementation Details**
The test suite expanded to 13 tests (`AssetApprovalSeleniumTest.java`) to cover the advanced authentication and file handling edge cases.

**5. Execution / Working**
Jenkins triggers `mvn test -P selenium` targeting the live Tomcat staging server (`http://localhost:8081/digital-asset-approval-platform`). It acts exactly like a human user.

**6. Verification / Result**
13/13 Selenium tests pass successfully, confirming 100% functionality of the deployed application.

**7. Evidence / Screenshot**
Figure 7: Selenium Test Execution Results
*Evidence: This screenshot demonstrates the 13 passing E2E tests executing against the staging server.*

**8. Viva Explanation**
"Unit tests aren't enough. We added Selenium to simulate real browser clicks. It logs in, uploads a real file, logs in as admin, and approves it, fully verifying the staging environment automatically."

---

## 14. TASK 10 - Quality Gates and Continuous Testing

**1. Objective / Requirement**
"Quality Gates: Failure gating (Build #5 vs Build #6). Automated abort of production deployment upon any test failure." (Source: backlog.md)

**2. Solution Implemented**
Integrated the Selenium tests directly into the `Jenkinsfile`. If any Selenium test fails, the pipeline immediately aborts and marks the build `FAILURE`, preventing bad code from proceeding to Docker/Production.

**3. Technologies / Tools Used**
Jenkins Pipeline, Selenium

**4. Implementation Details**
Verified with an intentional failure (Build #5), which skipped the Docker and Ansible deployment stages. Subsequently fixed and verified green in Build #6.

**5. Execution / Working**
The pipeline evaluates test exits. Non-zero exits cause immediate termination of the downstream deployment lifecycle.

**6. Verification / Result**
The Quality Gate actively protects the upstream artifacts. Jenkins Build #16 proves a successful full-pass gate transition.

**7. Evidence / Screenshot**
Figure 8: Jenkins Quality Gate Blocked Pipeline (Build 5)
*Evidence: This screenshot demonstrates the pipeline actively blocking a flawed deployment.*

**8. Viva Explanation**
"This is our safety net. If a developer breaks the UI, Selenium fails, and the Jenkins pipeline instantly aborts. The flawed code never makes it to Docker or Ansible. We proved this in Build #5."

---

## 15. TASK 11 - Docker Containerization

**1. Objective / Requirement**
"Docker Containerization: Dockerfile, image build/tagging, container lifecycle." (Source: backlog.md)

**2. Solution Implemented**
Created a multi-stage-capable `Dockerfile` based on `eclipse-temurin:17-jre`. Configured persistent volumes so uploaded assets survive container restarts.

**3. Technologies / Tools Used**
Docker Engine, Docker CLI

**4. Implementation Details**
The `Dockerfile` copies the WAR file from `target/` and executes it. Port 8080 inside the container is exposed. Persistent volume maps `/app/uploads`.

**5. Execution / Working**
Docker builds an immutable image of the verified application (`digital-asset-approval-platform:1.0.16`).

**6. Verification / Result**
The container runs robustly on host port 8082, handling real file uploads without losing data on restarts.

**7. Evidence / Screenshot**
Figure 9: Docker Container Status
*Evidence: This screenshot demonstrates the application running continuously within a Docker container.*

**8. Viva Explanation**
"To eliminate 'it works on my machine' problems, we Dockerized the app. The `Dockerfile` packages the JRE and our verified WAR file into a single, immutable container image."

---

## 16. TASK 12 - Local Registry and Docker CD

**1. Objective / Requirement**
"Local Registry & Docker CD: Local Docker registry on port 5000, Jenkins Docker CD." (Source: backlog.md)

**2. Solution Implemented**
Launched a private Docker registry (`registry:2`) on port 5000. Extended the Jenkins pipeline to automatically build, tag (`1.0.${BUILD_NUMBER}`), and push images to this registry.

**3. Technologies / Tools Used**
Docker Registry, Jenkins Pipeline

**4. Implementation Details**
The `Jenkinsfile` stages include `Docker Build`, `Docker Tag`, `Docker Push`, and `Docker Deploy`. Deployment removes the old container and boots the newly tagged one.

**5. Execution / Working**
Once Selenium passes, Jenkins builds the image, pushes it to `localhost:5000`, and triggers the production Docker host to pull and run it.

**6. Verification / Result**
Registry successfully holds versions (e.g., `1.0.16`, `latest`). The deployment container runs on port 8082.

**7. Evidence / Screenshot**
Figure 10: Jenkins Docker Build and Push Stage
*Evidence: This screenshot demonstrates the automated continuous deployment of the Docker image.*

**8. Viva Explanation**
"We deployed a private Registry on port 5000. If tests pass, Jenkins automatically tags the image with the build number, pushes it to the registry, and re-deploys the production container."

---

## 17. TASK 13 - Ansible Configuration Management

**1. Objective / Requirement**
"Ansible Configuration Management: Containerized Ansible controller, Ubuntu target, supervisor daemon." (Source: backlog.md)

**2. Solution Implemented**
Implemented infrastructure-as-code using Ansible. Created `inventory.ini` and `site.yml` to provision a clean target machine (simulated on port 8083).

**3. Technologies / Tools Used**
Ansible, YAML

**4. Implementation Details**
Ansible playbooks install dependencies (Java), configure a non-root user (`daap`), set up the directory structure (`/opt/daap`), and establish `supervisor` for process management.

**5. Execution / Working**
Jenkins executes `ansible-playbook -i ansible/inventory.ini ansible/site.yml`. Ansible SSHs into the target node and applies the state.

**6. Verification / Result**
First run successfully provisioned the bare target. The application is running stably and responding 200 OK on port 8083.

**7. Evidence / Screenshot**
Figure 11: Ansible Provisioning (Initial Run)
*Evidence: This screenshot demonstrates the successful initial configuration of the target node.*

**8. Viva Explanation**
"We used Ansible to ensure our target servers are configured exactly right. The playbook installs Java, sets up directories, and deploys the application automatically without human intervention."

---

## 18. TASK 14 - Idempotency and Rollback

**1. Objective / Requirement**
"Idempotency & Rollback: Idempotency verification, rollback playbook." (Source: backlog.md)

**2. Solution Implemented**
Validated that running the Ansible playbook multiple times results in `changed=0`. Created a `rollback.yml` playbook to instantaneously revert a bad release using symlinks.

**3. Technologies / Tools Used**
Ansible

**4. Implementation Details**
Ansible uses the `/opt/daap/releases/` pattern. A `/opt/daap/current` symlink points to the active release. Rollback simply repoints the symlink and restarts the daemon.

**5. Execution / Working**
If a flaw is discovered post-deployment, executing `rollback.yml` instantly switches traffic back to the prior stable version.

**6. Verification / Result**
Idempotency verified (`changed=0`). Rollback verified by repointing to a prior version successfully.

**7. Evidence / Screenshot**
Figure 12: Ansible Idempotency Output
*Evidence: This screenshot demonstrates the idempotency of the configuration management script (changed=0).*

**8. Viva Explanation**
"Idempotency means running the script twice changes nothing the second time. We verified this (changed=0). For disaster recovery, our rollback playbook repoints a symlink to instantly revert to the previous version."

---

## 19. TASK 15 - Final Pipeline and Documentation

**1. Objective / Requirement**
"Final E2E Pipeline, Documentation & Viva Prep: 14-stage Jenkins pipeline, comprehensive documentation, live demonstration guide, viva defense readiness." (Source: backlog.md)

**2. Solution Implemented**
Linked all 14 stages perfectly in Jenkins: Git -> Maven -> Tomcat -> Selenium -> Docker Build -> Tag -> Push -> Deploy -> Ansible -> Health Check. Finalized all documentation.

**3. Technologies / Tools Used**
Markdown, Jenkins, Full Stack

**4. Implementation Details**
Created this exact document (`TASK_WISE_DOCUMENTATION_1_TO_15.md`), alongside `VIVA_QA.md` and `FINAL_PROJECT_DEMONSTRATION_GUIDE.md`. The pipeline completed Build #16.

**5. Execution / Working**
A single Git push cascades through all 14 stages perfectly without any human intervention.

**6. Verification / Result**
All requirements are completely verified. Metrics are updated to accurately reflect the 19 Unit Tests and 13 Selenium tests.

**7. Evidence / Screenshot**
Figure 13: Jenkins Final 14-Stage Success
*Evidence: This screenshot demonstrates the completely unified continuous deployment lifecycle.*

**8. Viva Explanation**
"Task 15 tied everything together. We have a unified, 14-stage CI/CD pipeline that builds, tests, containerizes, and provisions our infrastructure automatically. The project is fully documented and ready for production."

---

## 20. Final Compliance Matrix

| TASK | ORIGINAL REQUIREMENT | IMPLEMENTED SOLUTION | EVIDENCE | STATUS |
|:---:|---|---|---|:---:|
| **1** | Define problem statement, target users, and constraints. | Defined roles, file types, 10MB limit in README/Backlog. | `README.md`, Backlog | **PASS** |
| **2** | Define Backlog, User Stories, and Agile Plan. | Created US-01 to US-06 and 15-week sprint plan. | `docs/backlog.md` | **PASS** |
| **3** | Core Architecture: Java 17, Spring Boot, Web UI. | Bootstrapped Java/Spring WAR, created MVC structure. | Application Startup | **PASS** |
| **4** | Git Foundation & Branching Strategy. | Setup Git, `.gitignore`, and `develop` branching model. | GitHub History | **PASS** |
| **5** | Asset Submission & Validation. | Built multipart form, UUID logic, and size/type validation. | Unit Tests (19/19) | **PASS** |
| **6** | Review Workflows & MVP Completion. | Added APPROVED/REJECTED states, comments, audit logs. | MVP running | **PASS** |
| **7** | Jenkins CI Automation. | Created Job with SCM polling, Maven test, JUnit archiving. | Jenkins Job Config | **PASS** |
| **8** | Declarative Jenkinsfile & Tomcat Staging. | Pushed WAR to Tomcat 8081 via declarative script. | Jenkinsfile, Port 8081 | **PASS** |
| **9** | Automated E2E Testing (Selenium). | Headless Chrome simulating full user/admin workflows. | 13/13 Selenium Pass | **PASS** |
| **10** | Quality Gates. | Pipeline aborts if Selenium or Unit tests fail. | Build #5 abort log | **PASS** |
| **11** | Docker Containerization. | Created Dockerfile exposing 8080 and `/app/uploads` volume. | Docker Image Build | **PASS** |
| **12** | Local Registry & Docker CD. | Registry on 5000; Jenkins auto builds/tags/pushes/deploys. | Port 8082, Port 5000 | **PASS** |
| **13** | Ansible Configuration Management. | `inventory.ini` and `site.yml` provision target with Java. | Target port 8083 | **PASS** |
| **14** | Idempotency & Rollback. | Verified `changed=0`; created symlink-based `rollback.yml`. | Ansible Logs | **PASS** |
| **15** | Final Pipeline & Documentation. | Full 14 stages linked; created comprehensive task reports. | Build #16, Docs | **PASS** |

---

## 21. Technology Matrix

| Technology | Purpose | Where Used |
|---|---|---|
| **Java 17 LTS** | Core backend language | Application runtime |
| **Spring Boot 3.1.2** | Backend framework (MVC, REST) | Controllers, Services |
| **HTML/CSS/JS** | Frontend UI rendering | `src/main/resources/static/` |
| **Apache Maven** | Build tool, Dependency resolution | `pom.xml`, Jenkins Pipeline |
| **Git / GitHub** | Version control and remote repository | `.git`, `develop` branch |
| **JUnit 5 / Mockito** | Backend Unit Testing | `src/test/` |
| **Selenium WebDriver** | Automated E2E Browser Testing | `AssetApprovalSeleniumTest.java` |
| **Jenkins** | CI/CD Orchestration Engine | Port 8080, `Jenkinsfile` |
| **Apache Tomcat 10.1** | Staging Application Server | Port 8081 |
| **Docker Engine** | Production Containerization | `Dockerfile`, Port 8082 |
| **Docker Registry** | Image Repository | Port 5000 |
| **Ansible** | Configuration Management (IaC) | `ansible/`, Target Port 8083 |

---

## 22. Complete Test Results

**Final Verified Metrics**:
- **Unit Tests**: 19 / 19 passed (`AssetRequestServiceTest.java`)
- **Selenium Tests**: 13 / 13 passed (`AssetApprovalSeleniumTest.java`)
- **Total Automation**: 32 / 32 passed (100% Quality Gate Success)
- **Latest Build**: Jenkins Build #16 (**SUCCESS**)
- **Target Commit**: `99484a2b859c4efb7e332e3e27bab75a51cce082`

---

## 23. Professor Demonstration Guide

**Step-by-Step Live Demo:**
1. Open application on deployed URL (e.g. `http://localhost:8082`).
2. Click **Login** and sign in as `USER`.
3. Submit a real asset (e.g. a `.jpg` or `.pdf` under 10MB) via the dashboard.
4. Verify the UI updates to show the asset as `PENDING`.
5. Click **Logout**.
6. Click **Login** and sign in as `ADMIN`.
7. View the pending queue to see the new asset.
8. Open the asset review modal.
9. Click to preview or download the actual file.
10. Click **Request Changes**, providing feedback in the comment box.
11. **Logout** and **Login** back as `USER`.
12. Show the notification/audit feedback left by the admin.
13. Resubmit the asset with corrections.
14. **Logout** and **Login** as `ADMIN`.
15. Select the resubmitted asset and click **Approve**.
16. Show the asset is now `APPROVED`.
17. Open the Audit History to show the immutable log of all actions.
18. Open **Jenkins (Port 8080)**.
19. Show **Build #16**, demonstrating all 14 green stages.
20. Show the Selenium test report with **13/13 passes**.
21. Run `docker ps` to show the live container and registry.
22. Show the Local Docker Registry catalog JSON.
23. Run the Ansible playbook again to demonstrate **idempotency** (`changed=0`).
24. Explain how this proves the end-to-end automated DevOps lifecycle.

---

## 24. Viva Questions and Answers

* **Why did you use Jenkins?**
  Jenkins acts as our orchestration engine. It watches our Git repository and automates the entire compilation, testing, and deployment lifecycle, completely eliminating manual human intervention and reducing human error.
* **Why Docker?**
  Docker provides an immutable artifact. It ensures that the exact same application environment that passed our tests is packaged and shipped to production, eliminating the classic "it works on my machine" problem.
* **Why Docker Registry?**
  A private registry allows us to store our versioned image artifacts locally and securely, acting as the distribution hub between our Jenkins CI build and our production Docker hosts.
* **Why Ansible?**
  Ansible implements "Infrastructure as Code." It allows us to declaratively script the exact configuration of our target servers, ensuring they are identical, reproducible, and version-controlled.
* **What is CI/CD in this project?**
  Continuous Integration is the automatic compilation and JUnit testing triggered by Git pushes. Continuous Delivery/Deployment is the automated transition of that code through Tomcat staging, Selenium testing, Dockerization, and Ansible provisioning.
* **What is the Selenium quality gate?**
  Our Jenkins pipeline explicitly checks the exit code of our 13 Selenium tests. If any test fails, the pipeline immediately aborts, preventing flawed code from ever being built into a Docker image or deployed to production.
* **What happens when Selenium fails?**
  As proven in our Build #5 log, a failure triggers an immediate build `FAILURE`, skips all deployment stages, and alerts the developers that the UI is broken.
* **How is role-based access implemented?**
  The platform utilizes active authentication tokens and enforces strict separation between `requesterId` operations and `reviewerId` operations in both the UI and the backend controllers.
* **How does the user/admin workflow work?**
  Users submit assets (`PENDING`). Admins review them and issue decisions (`APPROVED`, `REJECTED`, or `CHANGES_REQUESTED`). Users can resubmit if changes are required.
* **How are uploaded assets stored?**
  Files are written physically to an `uploads/` directory on the server. In Docker, this is mounted as a persistent volume (`daap-uploads:/app/uploads`) so data survives container restarts.
* **How is filename collision prevented?**
  During upload, the backend generates a random UUID for the file and prepends it to the original filename before saving it to the disk.
* **How is upload size validated?**
  The Spring Boot configuration (`application.properties`) strictly caps `multipart.max-file-size` to 10MB, throwing an exception if exceeded, which is handled gracefully by the UI.
* **How does admin download the real asset?**
  The admin clicks download, and the UI fetches the file via a secured backend endpoint (`/api/requests/{id}/file`) passing the authentication token via query parameter or header, preserving the original file type and name.
* **How does feedback reach the user?**
  When an admin requests changes, their comments are written to the `AuditLog`. The user dashboard dynamically fetches these audit records to display the feedback history for that asset.
* **How does resubmission work?**
  A user selects an asset in `CHANGES_REQUESTED` state and submits a new file. The backend processes the new upload and resets the asset state back to `PENDING`.
* **What is idempotency in Ansible?**
  Idempotency means that executing a playbook multiple times has the exact same result as executing it once. If the target server is already perfectly configured, Ansible will report `changed=0` and make no modifications.
* **How does rollback work?**
  Our deployment strategy places distinct application versions in `/opt/daap/releases/`. An active symlink (`/opt/daap/current`) points to the live version. A rollback instantly changes this symlink to point to the prior version and restarts the service.
* **Why is Tomcat on 8081?**
  We isolated ports to prevent conflicts on a single host. Jenkins runs on 8080, so Tomcat staging requires 8081 to run simultaneously.
* **Why is Docker on 8082?**
  To allow Tomcat (8081) and the Docker production container to run on the same physical machine without port conflicts.
* **What is the role of the local registry?**
  It runs on port 5000 and serves as the secure, local artifact repository for our Docker images, preventing reliance on public Docker Hub for proprietary enterprise code.
* **What happens from Git push to deployment?**
  A push triggers Jenkins -> Maven compilation -> Unit Tests -> WAR packaged -> Deployed to Tomcat -> Selenium verifies Tomcat -> Quality Gate passes -> Docker image built/tagged -> Pushed to registry -> Old container destroyed -> New container started -> Ansible configures target -> Health verified.

---

## 25. Final Conclusion
The **Automated Digital Asset Approval Platform** successfully fulfills all 15 task requirements. It provides a robust, role-secured business application underpinned by an enterprise-grade, fully automated CI/CD lifecycle. Through rigorous testing (19 unit, 13 E2E), strict quality gates, Docker containerization, and idempotent Ansible configuration management, the system demonstrates the highest standards of modern software engineering and DevOps practices.
