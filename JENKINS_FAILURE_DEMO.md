# Jenkins Controlled Failure Gate Demonstration

## 1. Exact File, Line, and Defect to Introduce
* **File**: `src/main/resources/static/index.html`
* **Line**: ~523 (inside `handleAssetSubmit()` function)
* **Change**:
  Change:
  ```javascript
  feedback.textContent = `Asset "${result.title}" submitted successfully! ID: ${result.id}`;
  ```
  To:
  ```javascript
  feedback.textContent = `Asset failed submission deliberately`;
  ```

## 2. Expected Selenium Test Failure
* Test `test1_AssetSubmissionSuccess` in `AssetApprovalSeleniumTest.java` will fail assertion:
  ```
  AssertionFailedError: Expected success message, but got: Asset failed submission deliberately
  ```
* Automatic failure screenshot is generated and saved to:
  `target/selenium-screenshots/test1_AssetSubmissionSuccess_FAILURE_<timestamp>.png`

## 3. Expected Jenkins Pipeline Behavior
1. Stage `Selenium Tests` exits with code `1` and marks stage as **FAILED**.
2. JUnit test results publish 1 failure.
3. Jenkins archives the failure screenshot artifact.
4. Stage `Production Deployment` evaluates:
   ```groovy
   when {
       allOf {
           expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
           expression { params.DEPLOY_ENV == 'production' }
       }
   }
   ```
   Since `currentBuild.result` is `FAILURE`, **Production Deployment is SKIPPED**.
5. Overall Jenkins pipeline status is **FAILURE (RED)**.

## 4. Exact Fix to Restore
Restore line in `src/main/resources/static/index.html`:
```javascript
feedback.textContent = `Asset "${result.title}" submitted successfully! ID: ${result.id}`;
```
Rebuilding and rerunning will result in all 5 Selenium tests passing and a green pipeline.
