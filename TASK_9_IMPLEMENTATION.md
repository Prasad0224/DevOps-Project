# Task 9 Implementation: Selenium WebDriver & JUnit E2E Test Suite

## Overview
Task 9 provides automated end-to-end (E2E) browser verification using Selenium WebDriver and JUnit 5, testing real user workflows against the application UI deployed to Apache Tomcat 10.1.60.

**Verified Task Status**: **PASS**  
**Tests Executed**: 5/5 Passing (0 Failures, 0 Errors, 0 Skipped)  
**Execution Time**: ~11.5 seconds  
**Target Environment**: Apache Tomcat 10.1.60 at `http://localhost:8081/digital-asset-approval-platform`

---

## 1. Test Suite Architecture
- **Test Class**: `src/test/java/com/platform/selenium/AssetApprovalSeleniumTest.java`
- **Dependencies**:
  - `org.seleniumhq.selenium:selenium-java` (version 4.16.1)
  - `io.github.bonigarcia:webdrivermanager` (version 5.6.3)
  - `org.junit.jupiter:junit-jupiter` (JUnit 5)

---

## 2. Test Cases Implemented & Verified

| # | Test Method Name | Priority Area | Verified Behavior |
|---|---|---|---|
| 1 | `test1_AssetSubmissionSuccess` | Asset Submission & Queue | Submits a valid asset with title, description, file name, size, and requester ID; verifies success feedback alert and request entry in the queue with `PENDING` status. |
| 2 | `test2_AssetSubmissionValidationFailure` | Client Validation (File Type & Size) | Attempts submission with invalid/empty fields, verifying rejection of unsupported extensions or missing titles. |
| 3 | `test3_DashboardDataRender` | Live Dashboard & Metrics | Verifies that the queue table properly renders submitted asset records with ID, title, requester, file size, and status badges. |
| 4 | `test4_ApproveAssetWorkflow` | Reviewer Approval Workflow | Submits a request, clicks Review action button, enters reviewer ID and approval comment, submits approval, and verifies badge updates to `APPROVED`. |
| 5 | `test5_RejectAssetWorkflow` | Reviewer Rejection & Audit | Submits a request, executes reviewer rejection with mandatory audit reason, and verifies badge updates to `REJECTED`. |

---

## 3. Key Technical Capabilities
1. **Configurable Base URL**: Checks `-Dapp.baseUrl`, then environment variable `APP_BASE_URL`, defaulting to `http://localhost:8080`. Jenkins passes `-Dapp.baseUrl=http://localhost:8081/digital-asset-approval-platform`.
2. **Resilient Initialization**: Includes a navigation retry loop in `@BeforeEach loadApp()` to accommodate dynamic container redeployment and context warm-up.
3. **Headless Chrome Execution**: Automatically configured for CI agents with `--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`, and window dimensions `1920x1080`.
4. **Explicit Waits**: Employs `WebDriverWait` (20s timeout) with `ExpectedConditions` (`visibilityOfElementLocated`, `textToBePresentInElement`, `presenceOfElementLocated`) avoiding brittle thread sleeps.
5. **Automated Screenshot on Failure**: Utilizes a JUnit 5 `TestWatcher` callback that captures a full-page PNG screenshot to `target/selenium-screenshots/<testName>_FAILURE_<timestamp>.png` whenever any test fails.

---

## 4. Execution Command
To execute the Selenium E2E suite against the running Tomcat instance:
```bash
mvn test -Pselenium -Dapp.baseUrl=http://localhost:8081/digital-asset-approval-platform -Dselenium.headless=true
```
Execution log from Jenkins Build #6:
```
[INFO] Running com.platform.selenium.AssetApprovalSeleniumTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 11.51 s -- in com.platform.selenium.AssetApprovalSeleniumTest
[INFO] Results:
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
