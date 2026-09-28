# Task 8 Implementation: Declarative Jenkinsfile & Tomcat Deployment Pipeline

## Overview
A declarative `Jenkinsfile` at the repository root defines the complete CI/CD lifecycle across automated checkout, compilation, unit testing, packaging, archiving, and parameterized deployment.

## 1. Pipeline Stages
The pipeline enforces sequential execution:
1. **Checkout**: Checks out source code from GitHub SCM repository.
2. **Build**: Compiles source code with `mvn compile`.
3. **Unit Tests**: Executes unit test suite (`mvn test`) and publishes JUnit XML test reports (`**/surefire-reports/*.xml`).
4. **Package**: Builds the Tomcat-ready WAR package (`mvn package -DskipTests`).
5. **Archive**: Archives `target/*.war` with fingerprinting for traceability.
6. **Deploy (Test/Staging)**: Deploys the WAR to the selected environment directory (`deployments/${params.DEPLOY_ENV}`).

## 2. Environment Parameterization
The pipeline defines parameters at launch:
- `DEPLOY_ENV`: Choice parameter (`test`, `staging`, `production`), defaulting to `test`.
- `APP_PORT`: Configurable HTTP port (default `8080`).
- `RUN_SELENIUM_E2E`: Boolean parameter allowing automated regression testing on demand.

## 3. Security & Zero Hardcoded Secrets
- No credentials or sensitive tokens are stored in the `Jenkinsfile` or repository.
- Tomcat deployment directories and credentials leverage Jenkins environment variables and Jenkins Credentials Store bindings (`withCredentials`).

## 4. Pipeline Failure Handling
- Any step or command returning a non-zero exit code triggers an immediate pipeline failure.
- When an upstream stage fails, all downstream stages (including Deployments) are aborted.
- Post-action blocks notify pipeline status and preserve test result artifacts.

## 5. OS Compatibility
- Windows and Linux compatible using `isUnix()` checks:
  - Linux/macOS: runs shell commands via `sh`.
  - Windows: runs batch commands via `bat`.
