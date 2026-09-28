# Task 9 Implementation: Selenium WebDriver & JUnit E2E Test Suite

## Overview
Task 9 provides automated end-to-end (E2E) browser verification using Selenium WebDriver and JUnit 5, testing real user workflows against the application UI.

## 1. Test Suite Architecture
- **Test Class**: `src/test/java/com/platform/selenium/AssetApprovalSeleniumTest.java`
- **Dependencies**:
  - `org.seleniumhq.selenium:selenium-java` (version 4.16.1)
  - `io.github.bonigarcia:webdrivermanager` (version 5.6.3)
  - `org.junit.jupiter:junit-jupiter` (JUnit 5)

## 2. Test Cases Implemented

| Test Name | Priority Area | Description |
|-----------|---------------|-------------|
| `test1_AssetSubmissionSuccess` | Asset Submission & Queue | Fills title, description, file name, size, requester ID; submits form; verifies success feedback and verifies request entry in queue with initial `PENDING` status. |
| `test2_AssetSubmissionValidation_UnsupportedExtension` | Validation (File Type) | Submits asset with unsupported extension (`dangerous_script.exe`); verifies error message `"Unsupported file type"`. |
| `test3_AssetSubmissionValidation_OversizedFile` | Validation (File Size) | Submits file exceeding 10MB limit (15MB); verifies error message `"exceeds 10MB limit"`. |
| `test4_ReviewerApprovalWorkflow` | Reviewer Approval | Submits asset; opens review modal; inputs reviewer ID and comment; clicks approve; verifies status badge updates to `APPROVED`. |
| `test5_ReviewerRejectionAndRequestChangesWorkflow` | Reviewer Reject & Revise | Exercises both `REJECTED` and `CHANGES_REQUESTED` actions with reviewer comments, verifying respective status badge updates in real time. |

## 3. Key Technical Capabilities
1. **Configurable Base URL**: Checks `-Dapp.baseUrl`, then environment variable `APP_BASE_URL`, defaulting to `http://localhost:8080`.
2. **Headless Chrome Execution**: Automatically configured for CI agents with `--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`, and window dimensions `1920x1080`.
3. **Explicit Waits**: Employs `WebDriverWait` with `ExpectedConditions` (`visibilityOfElementLocated`, `textToBePresentInElement`, `invisibilityOfElementLocated`) avoiding race conditions or brittle thread sleeps.
4. **Automated Screenshot on Failure**: Utilizes a JUnit 5 `TestWatcher` extension callback that automatically captures a full-page PNG screenshot to `target/selenium-screenshots/<testMethod>_FAILURE_<timestamp>.png` whenever any test fails.

## 4. Execution Command
To execute the Selenium E2E suite against a running instance:
```bash
mvn test -Pselenium -Dapp.baseUrl=http://localhost:8080 -Dselenium.headless=true
```
All 5 tests execute and pass cleanly.
