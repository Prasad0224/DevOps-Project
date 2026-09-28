# Task 8 Implementation: Declarative Jenkinsfile & Tomcat Deployment Pipeline

## Overview
A declarative `Jenkinsfile` at the repository root defines the complete CI/CD lifecycle across automated checkout, compilation, unit testing, packaging, archiving, Tomcat staging deployment, Selenium regression testing, and production artifact promotion.

**Verified Task Status**: **PASS**  
**Jenkins Pipeline**: `digital-asset-approval-platform-pipeline` (Build #6 **SUCCESS**)  
**Tomcat Runtime**: Apache Tomcat 10.1.60 on HTTP port **8081**  
**Application URL**: [http://localhost:8081/digital-asset-approval-platform](http://localhost:8081/digital-asset-approval-platform)  
**Health Check URL**: [http://localhost:8081/digital-asset-approval-platform/api/health](http://localhost:8081/digital-asset-approval-platform/api/health)  

---

## 1. Pipeline Stages
The pipeline enforces sequential execution across 8 stages:
1. **Checkout**: Checks out source code from GitHub SCM repository (`develop` branch).
2. **Build**: Compiles source code with `mvn compile`.
3. **Unit Tests**: Executes unit test suite (`mvn test`) and publishes JUnit XML test reports (`**/surefire-reports/*.xml`).
4. **Package**: Builds the Tomcat-ready WAR package (`mvn package -DskipTests`).
5. **Archive**: Archives `target/*.war` with fingerprinting for traceability.
6. **Test/Staging Deployment**: Deploys the WAR to Tomcat webapps (`C:\apache-tomcat-10.1.60\webapps\digital-asset-approval-platform.war`), waits for auto-reloading, and verifies HTTP 200 on `/api/health`.
7. **Selenium Tests**: Executes automated headless Chrome E2E tests against the Tomcat deployment.
8. **Production Deployment**: Once Selenium passes, promotes the verified WAR to `deployments/production/`.

---

## 2. Environment Parameterization
The pipeline defines parameters at launch:
- `DEPLOY_ENV`: Target environment (default `production`).
- `TOMCAT_HOST`: Tomcat host (default `localhost`).
- `TOMCAT_PORT`: Tomcat HTTP connector port (default `8081`).
- `TOMCAT_CONTEXT_PATH`: Context root (default `digital-asset-approval-platform`).
- `TOMCAT_WEBAPPS_DIR`: Tomcat deployment directory (default `C:\apache-tomcat-10.1.60\webapps`).
- `RUN_SELENIUM_E2E`: Boolean flag (default `true`) controlling automated regression testing.

---

## 3. Production Artifact Promotion
In this single-machine academic DevOps project, there is no remote production server. The pipeline stage labeled **Production Deployment** represents **Production Artifact Promotion**:
- It validates that all preceding stages (specifically Stage 7 Selenium Tests) succeeded.
- It copies the verified WAR artifact into the release directory `deployments/production/digital-asset-approval-platform.war` on the Jenkins workspace.
- It guarantees that an unverified or failing artifact is never promoted to the release directory.

---

## 4. Pipeline Failure Handling
- Any step or command returning a non-zero exit code triggers an immediate pipeline failure.
- When an upstream stage fails, all downstream stages (including Production Promotion) are aborted.
- Post-action blocks notify pipeline status and preserve test result artifacts and failure screenshots.

---

## 5. OS Compatibility
- Windows and Linux compatible using `isUnix()` checks:
  - Linux/macOS: runs shell commands via `sh`.
  - Windows: runs batch commands via `bat` with standardized Windows path syntax.
