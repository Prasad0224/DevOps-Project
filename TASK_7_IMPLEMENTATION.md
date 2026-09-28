# Task 7 Implementation: Jenkins CI Job Configuration & Maven Automation

## Overview
Task 7 establishes continuous integration automation for the Digital Asset Approval Platform using Maven and Jenkins CI.

## 1. Maven Build & Packaging Configuration
The build configuration in `pom.xml` was upgraded to produce a standard web archive (WAR) artifact suitable for Apache Tomcat:
- **Packaging**: `<packaging>war</packaging>`
- **Final Name**: `digital-asset-approval-platform.war`
- **Servlet Container**: `spring-boot-starter-tomcat` included with `<scope>provided</scope>`
- **Servlet Initializer**: `DigitalAssetApprovalApplication.java` extends `SpringBootServletInitializer` and overrides `configure()`, enabling both standalone `java -jar` and servlet container deployment.

## 2. Automated Test Execution
- **Surefire Configuration**: `maven-surefire-plugin` (version 3.1.2) executes unit tests during the standard test phase (`mvn test`).
- **Reports**: Test XML reports are written to `target/surefire-reports/TEST-com.platform.service.AssetRequestServiceTest.xml`.
- **Test Results**: 7 unit test methods covering submission validation, file size/type constraints, and all reviewer actions (`APPROVED`, `REJECTED`, `CHANGES_REQUESTED`) execute with 0 failures and 0 errors.

## 3. Artifact Archiving
- Build outputs generated at `target/digital-asset-approval-platform.war` are archived in Jenkins using the `archiveArtifacts` pipeline step with fingerprinting enabled.

## 4. GitHub SCM & Polling Triggers
- **SCM Integration**: Configured in Jenkins pipeline via `checkout scm`, pulling from `https://github.com/Prasad0224/DevOps-Project.git`.
- **Trigger**: Pipeline configured with SCM polling trigger `pollSCM('H/5 * * * *')` to automatically detect upstream commits every 5 minutes, as well as webhook triggers (`githubPush()`).
