# Task 10 Implementation: Selenium CI/CD Integration & Production Release Gate

## Overview
Task 10 incorporates automated browser-based Selenium tests as a strict quality gate in the Jenkins pipeline between Staging and Production deployment.

## 1. Complete CI/CD Pipeline Flow

```
[Checkout]
    ↓
[Build] (mvn compile)
    ↓
[Unit Tests] (mvn test) ───[JUnit Reports Published]
    ↓
[Package] (Tomcat WAR)
    ↓
[Archive] (target/*.war Fingerprinted)
    ↓
[Test/Staging Deployment] (Deployed to Tomcat Test/Staging)
    ↓
[Selenium E2E Tests] ─────┬─── [FAILED] ──→ Pipeline Aborted (Production Skipped, Screenshots Archived)
                          └─── [PASSED] ──→ [Production Deployment Gate]
                                                 ↓
                                            [Production Deployment]
```

## 2. Production Gate Enforcement
The `Production Deployment` stage is gated by two conditions:
1. `currentBuild.result == null || currentBuild.result == 'SUCCESS'` (ensuring no previous stage, including Selenium, failed).
2. `params.DEPLOY_ENV == 'production'`.

If Selenium fails:
- The `Selenium Tests` stage catches the failure.
- `archiveArtifacts` archives all screenshots in `target/selenium-screenshots/**`.
- `junit` records test failures.
- The pipeline aborts immediately, guaranteeing that broken builds never reach Production.

## 3. Controlled Failure Demonstration & Remediation

### Demonstration of Controlled Failure:
1. **Trigger Condition**:
   During initial testing, the feedback alert container in `src/main/resources/static/index.html` reset with `style.display = 'none'`, which prevented the CSS class `.feedback-success` from rendering as visible to the browser (`visibilityOfElementLocated`).
2. **Failure Result**:
   Surefire reported:
   ```
   [ERROR] AssetApprovalSeleniumTest.test1_AssetSubmissionSuccess:130
   Timeout Expected condition failed: waiting for visibility of element located by By.id: form-feedback
   ```
3. **Artifact Captured**:
   The JUnit `TestWatcher` automatically triggered and generated:
   `target/selenium-screenshots/test1_AssetSubmissionSuccess_FAILURE_20260928_222158.png`
4. **Pipeline Impact**:
   The stage failed, and the Production Deployment stage was skipped.

### Remediation:
1. **Fix Applied**:
   Updated `handleAssetSubmit()` in `index.html` to explicitly set `feedback.style.display = 'block'` upon receiving the server response.
2. **Verification**:
   Re-executed the full Selenium suite:
   ```
   [INFO] Running com.platform.selenium.AssetApprovalSeleniumTest
   [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.429 s
   [INFO] BUILD SUCCESS
   ```
   All 5 end-to-end tests passed, confirming the fix without deleting or weakening any test assertions.
