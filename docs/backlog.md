# Product Backlog & Agile Planning Specification

**Project**: Automated Digital Asset Approval Platform (DAAP)
**Release**: `v1.0.0-mvp` (Git Tag: `v1.0.0-mvp`)
**Methodology**: Agile Scrum / Kanban with 15-Week Milestone Cadence

---

## 1. Task 1: Problem Definition and Scope

### 1.1 Problem Statement
Enterprise creative, marketing, and engineering organizations exchange hundreds of digital assets (imagery, design specifications, release documentation) weekly. Traditional approval workflows rely on unstructured email threads, chat channels, and ad-hoc shared drives. This results in lost submissions, lack of auditability, unverified file constraints, security vulnerabilities, and protracted release delays.

### 1.2 Real-Time Need
Organizations require an automated, centralized, role-segregated portal to govern the submission, automated validation, and formal reviewer sign-off of digital assets prior to distribution and release.

### 1.3 Target Users & Roles
* **Requester (`requesterId`)**: Submits digital media assets, supplies mandatory metadata, reviews real-time status, and re-submits when changes are requested.
* **Reviewer (`reviewerId`)**: Inspects pending submissions, validates compliance, issues decisions (`APPROVED`, `REJECTED`, `CHANGES_REQUESTED`), and supplies mandatory audit rationale.
* **DevOps / System Administrator**: Oversees CI/CD automation, servlet containers, Docker images, and target host configuration management.

### 1.4 Existing Pain Points
1. **Unenforced Constraints**: Users upload corrupt, oversized, or unsupported file formats.
2. **Missing Audit Trails**: Compliance cannot trace who approved or rejected an asset or when.
3. **Status Opacity**: Requesters have no real-time visibility into whether reviews are pending, approved, or rejected.
4. **Manual Deployments**: Staging and release deployments are brittle, error-prone, and slow.

### 1.5 Stakeholders
* **Marketing & Creative Leads**: Ensure brand consistency and asset compliance.
* **Engineering & Product Managers**: Require rapid, traceable asset release lifecycles.
* **Compliance & Information Security**: Require role separation, non-root execution, and immutable audit logging.
* **DevOps Engineers**: Require automated CI/CD gating, containerization, and idempotent provisioning.

### 1.6 Constraints
* **File Size Constraint**: Maximum 10MB per uploaded asset file.
* **Supported File Types**: `.jpg`, `.jpeg`, `.png`, `.pdf`, `.docx`.
* **Runtime Constraints**: Java 17 LTS, Spring Boot 3.1.2, Apache Tomcat 10.1.x (Jakarta Servlet 6.0).
* **Port Allocations**: Non-conflicting local host ports (Jenkins `8080`, Tomcat `8081`, Docker `8082`, Ansible `8083`, Registry `5000`).

### 1.7 Measurable Success Criteria
* 100% automated rejection of oversized or unsupported files.
* Zero manual steps from code push to staging deployment.
* 100% test pass rate (7/7 unit tests, 5/5 Selenium E2E tests).
* Automated abort of production deployment upon any test failure (Quality Gate).
* Idempotent configuration management (`changed=0` on repeated runs).
* Sub-minute automated rollback to previous release upon failure.

### 1.8 Approved 15-Week MVP Scope
* Core Asset Submission Portal (Form, upload simulation, metadata).
* Server-side and Client-side Validation Engine.
* Reviewer Decision Engine (`APPROVED`, `REJECTED`, `CHANGES_REQUESTED`).
* Real-Time Dashboard & Status Tracking.
* In-Memory Audit Trail & Event Notification Service.

---

## 2. Product Backlog Status (Release v1.0.0-mvp)

| Priority | Item ID | Backlog Item | Target Scope | Status | Delivered In |
|:---:|:---:|---|---|:---:|:---:|
| 1 | BL-01 | User authentication & role management (SSO/LDAP) | Post-MVP | Planned | Post-v1.0.0 |
| 2 | BL-02 | Asset submission form + file upload handling | MVP Scope | **Done** | PR #1 |
| 3 | BL-03 | Document & metadata validation engine | MVP Scope | **Done** | PR #1 |
| 4 | BL-04 | Reviewer queue & decision workflows (`APPROVED`, `REJECTED`, `CHANGES_REQUESTED`) | MVP Scope | **Done** | PR #2 |
| 5 | BL-05 | Status tracking dashboard with live counters | MVP Scope | **Done** | PR #2 |
| 6 | BL-06 | Reviewer & requester event notification service | MVP Scope | **Done** | Commit `e50a7fd` |
| 7 | BL-07 | Immutable audit logging of all review actions | MVP Scope | **Done** | PR #2 |
| 8 | BL-08 | Automated CI/CD pipeline, Dockerization & Configuration Management | Full Scope | **Done** | Tasks 7–15 |

---

## 3. User Stories & Acceptance Criteria

### US-01: Asset Submission (Requester)
* **As a** Requester,
  **I want to** submit a digital asset with title, description, file details, and requester ID,
  **So that** my asset can be formally queued for reviewer evaluation.
* **Acceptance Criteria**:
  1. The form requires Title (min 3 chars), Requester ID, and a valid file.
  2. Accepted file formats: `.jpg`, `.jpeg`, `.png`, `.pdf`, `.docx`.
  3. Maximum allowable file size is 10 MB.
  4. On successful submission, asset is assigned a unique UUID and set to `PENDING` status.
  5. Requester receives a visual confirmation alert with the generated Request ID.

### US-02: Submission Validation Engine
* **As a** Governance System,
  **I want to** validate uploaded files and metadata before ingestion,
  **So that** defective or non-compliant files are rejected immediately.
* **Acceptance Criteria**:
  1. If file size > 10MB, return `400 Bad Request` with message: "File size exceeds 10MB limit".
  2. If file format is not supported (e.g. `.exe`, `.zip`), return `400 Bad Request` with message: "Unsupported file type".
  3. If title is blank, return `400 Bad Request` with message: "Title is required".
  4. Validation failure produces descriptive UI alert and logs warning.

### US-03: Reviewer Decision Workflow
* **As a** Reviewer,
  **I want to** review pending digital assets and approve, reject, or request changes with comments,
  **So that** content standards are enforced.
* **Acceptance Criteria**:
  1. Review modal displays asset details (Title, Requester, File Name, Size).
  2. Reviewer can select: `APPROVED`, `REJECTED`, or `CHANGES_REQUESTED`.
  3. Reviewer ID and comments are required fields.
  4. Decision updates asset status in real time and updates queue badges.

### US-04: Real-Time Status Tracking Dashboard
* **As a** Requester or Reviewer,
  **I want to** view a real-time dashboard with queue tables and metrics,
  **So that** I have immediate visibility into pending, approved, and rejected assets.
* **Acceptance Criteria**:
  1. Metric counter cards show counts for Total, Pending, Approved, and Rejected requests.
  2. Table dynamically lists ID, Title, Requester, File Name, Size, Status Badge, and Actions.
  3. Review Action button is enabled only for requests in `PENDING` or `CHANGES_REQUESTED` status.

### US-05: Immutable Audit Logging
* **As a** Compliance Auditor,
  **I want to** access a complete audit trail of every review action,
  **So that** decisions are transparent and non-repudiable.
* **Acceptance Criteria**:
  1. Every decision creates an `AuditLog` entry with ID, Request ID, Action, Reviewer ID, Comments, and Timestamp.
  2. Audit records are immutable and stored chronologically.

### US-06: Event Notification
* **As a** Requester,
  **I want to** receive notifications when my asset status transitions,
  **So that** I can promptly address requested changes or proceed with approved assets.
* **Acceptance Criteria**:
  1. Notification service logs event message on submission (`PENDING`).
  2. Notification service logs event message upon reviewer decision (`APPROVED`, `REJECTED`, `CHANGES_REQUESTED`).

---

## 4. 15-Week Agile Scrum & Kanban Plan

| Week(s) | Sprint | Focus Area | Deliverables & Milestones | Task Alignment |
|:---:|:---:|---|---|:---:|
| **Week 1–2** | Sprint 1 | Project Inception & Requirements | Problem definition, stakeholder alignment, user stories, architecture design, and initial backlog. | **Tasks 1–2** |
| **Week 3–4** | Sprint 2 | Core Architecture & Technology Setup | Java 17, Spring Boot 3, Maven build, initial domain models, REST controllers, and responsive web UI. | **Task 3** |
| **Week 5–6** | Sprint 3 | Git Foundation & Branching Strategy | Git repository setup, issue templates, branch policies (`develop`, `feature/*`, `release/*`), commit rules. | **Task 4** |
| **Week 7–8** | Sprint 4 | Feature 1: Asset Submission & Validation | Asset submission form, file constraint validation engine, unit tests (`AssetRequestServiceTest`), PR #1. | **Task 5** |
| **Week 9–10** | Sprint 5 | Feature 2: Review Workflows & MVP Completion | Reviewer actions (`APPROVED`/`REJECTED`/`CHANGES_REQUESTED`), audit trail, merge conflict resolution, PR #2, Tag `v1.0.0-mvp`. | **Task 6** |
| **Week 11** | Sprint 6 | Jenkins CI Automation & Tomcat Staging | Declarative `Jenkinsfile`, Maven CI automation, SCM polling, deployment to Apache Tomcat 10.1.60. | **Tasks 7–8** |
| **Week 12** | Sprint 7 | Automated E2E Testing & Quality Gates | Headless Chrome Selenium test suite (5 E2E tests), JUnit test reports, failure gating (Build #5 vs Build #6). | **Tasks 9–10** |
| **Week 13** | Sprint 8 | Docker Containerization & Local Registry | Dockerfile, image build/tagging, container lifecycle, local Docker registry on port 5000, Jenkins Docker CD. | **Tasks 11–12** |
| **Week 14** | Sprint 9 | Ansible Configuration Management & Rollback | Containerized Ansible controller, Ubuntu target (`daap-target`), supervisor daemon, idempotency verification, rollback playbook. | **Tasks 13–14** |
| **Week 15** | Sprint 10 | Final E2E Pipeline, Documentation & Viva Prep | 14-stage Jenkins pipeline, comprehensive documentation, live demonstration guide, viva defense readiness. | **Task 15** |

---

## 5. Definition of Done (DoD)

A backlog item or user story is considered **Done** only when all of the following criteria are met:
1. **Code Complete**: Implementation matches user story acceptance criteria with clean code standards.
2. **Unit Tested**: Unit test coverage exists with 100% pass rate (`mvn test`).
3. **Peer Reviewed**: Code submitted via pull request (`feature/* -> develop`), reviewed, and merged without unhandled merge conflicts.
4. **Automated CI/CD**: Passes compilation, packaging, and archiving in Jenkins without errors.
5. **E2E Verified**: Deployed to staging environment and verified via automated Selenium test suite (`AssetApprovalSeleniumTest`).
6. **Containerized & Provisioned**: Docker image built, pushed to registry, and verified via Ansible configuration management.
7. **Documented**: Release notes, backlog tracking, and architectural documentation updated.

---

## 6. DevOps Lifecycle Workflow

```
[Agile Planning] ──► [Local Dev / Git] ──► [Pull Request] ──► [Jenkins CI] ──► [Tomcat Staging]
     (Backlog)         (Feature Branches)       (Code Review)     (Compile & Test)     (Port 8081)
                                                                                          │
[Production / Target] ◄── [Ansible Config] ◄── [Docker Deploy] ◄── [Selenium Gate] ◄──────┘
      (Port 8083)           (Supervisor)           (Port 8082)         (5/5 E2E Tests)
```
