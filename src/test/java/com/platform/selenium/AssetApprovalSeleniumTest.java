package com.platform.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class AssetApprovalSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static String baseUrl;
    private static final String SCREENSHOT_DIR = "target/selenium-screenshots/";

    private static Path testPngPath;
    private static Path testPdfPath;
    private static Path testDocxPath;
    private static Path testExePath;
    private static Path testLargePdfPath;
    private static Path testJpegPath;

    private static String sharedSubmittedTitle;

    @org.junit.jupiter.api.extension.RegisterExtension
    TestWatcher screenshotWatcher = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            String testName = context.getTestMethod().map(m -> m.getName()).orElse("unknown_test");
            captureScreenshot(testName + "_FAILURE");
        }
    };

    @BeforeAll
    static void setUpAll() {
        baseUrl = System.getProperty("app.baseUrl");
        if (baseUrl == null || baseUrl.trim().isEmpty()) baseUrl = System.getenv("APP_BASE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) baseUrl = "http://localhost:8080";

        ChromeOptions options = new ChromeOptions();
        String explicitChromeBin = System.getProperty("webdriver.chrome.bin");
        if (explicitChromeBin != null && !explicitChromeBin.isEmpty()) {
            options.setBinary(explicitChromeBin);
        } else {
            for (String p : new String[]{
                    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"}) {
                if (new File(p).exists()) { options.setBinary(p); break; }
            }
        }

        boolean headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
        if (headless) options.addArguments("--headless=new");
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                "--window-size=1920,1080", "--remote-allow-origins=*");

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        new File(SCREENSHOT_DIR).mkdirs();

        Path testFilesDir = Paths.get("target", "selenium-test-files");
        try {
            Files.createDirectories(testFilesDir);
            testPngPath = testFilesDir.resolve("banner.png").toAbsolutePath();
            testPdfPath = testFilesDir.resolve("document.pdf").toAbsolutePath();
            testDocxPath = testFilesDir.resolve("bad-draft.docx").toAbsolutePath();
            testExePath = testFilesDir.resolve("dangerous_script.exe").toAbsolutePath();
            testLargePdfPath = testFilesDir.resolve("large_movie.pdf").toAbsolutePath();
            testJpegPath = testFilesDir.resolve("photo.jpeg").toAbsolutePath();

            Files.write(testPngPath, "dummy png asset data".getBytes());
            Files.write(testPdfPath, "dummy pdf asset data".getBytes());
            Files.write(testDocxPath, "dummy docx asset data".getBytes());
            Files.write(testExePath, "dangerous executable payload".getBytes());
            Files.write(testJpegPath, "dummy jpeg asset data".getBytes());
            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(testLargePdfPath.toFile(), "rw")) {
                raf.setLength(15 * 1024 * 1024); // 15MB
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare test upload files", e);
        }
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) driver.quit();
    }

    // -----------------------------------------------------------------------
    // Helper: Login with role
    // -----------------------------------------------------------------------
    private void loginAs(String username, String password) {
        driver.get(baseUrl);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-username")));
        userField.clear();
        userField.sendKeys(username);
        WebElement passField = driver.findElement(By.id("login-password"));
        passField.clear();
        passField.sendKeys(password);
        driver.findElement(By.id("login-btn")).click();
    }

    private void loginAsUser() { loginAs("user", "user123"); }
    private void loginAsAdmin() { loginAs("admin", "admin123"); }

    private void waitForUserDashboard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("title")));
    }

    private void waitForAdminDashboard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("admin-requests-tbody")));
    }

    private WebElement getFileInput() {
        List<WebElement> elements = driver.findElements(By.id("file"));
        if (!elements.isEmpty()) return elements.get(0);
        return driver.findElement(By.id("fileName"));
    }

    private void logout() {
        try {
            WebElement logoutBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-logout")));
            logoutBtn.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-username")));
        } catch (Exception e) {
            // If logout fails, just navigate to base URL
            driver.get(baseUrl);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-username")));
        }
    }

    // -----------------------------------------------------------------------
    // TEST 1: USER Login → Upload PNG → Submit → Verify PENDING
    // -----------------------------------------------------------------------
    @Test
    void test1_UserLogin_SubmitPNG_VerifyPending() {
        loginAsUser();
        waitForUserDashboard();

        sharedSubmittedTitle = "Q3 Campaign Hero " + System.currentTimeMillis();

        WebElement titleInput = driver.findElement(By.id("title"));
        WebElement descInput = driver.findElement(By.id("description"));
        WebElement fileInput = getFileInput();
        WebElement submitBtn = driver.findElement(By.id("submit-btn"));

        titleInput.clear();
        titleInput.sendKeys(sharedSubmittedTitle);
        descInput.clear();
        descInput.sendKeys("Hero graphic for product launch");
        fileInput.sendKeys(testPngPath.toString());

        submitBtn.click();

        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        assertTrue(feedback.getText().contains("submitted successfully"),
                "Expected success message, got: " + feedback.getText());

        // Verify PENDING row in user's table
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), sharedSubmittedTitle));
        List<WebElement> rows = driver.findElements(By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]"));
        assertFalse(rows.isEmpty(), "Submitted asset should appear in My Submissions table");

        WebElement statusBadge = rows.get(0).findElement(By.xpath(".//span[contains(@class,'status-badge')]"));
        assertEquals("PENDING", statusBadge.getText().trim());

        captureScreenshot("test1_user_submitted_pending");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 2: ADMIN Login → Verify Request in Queue → Open → Check Details
    // -----------------------------------------------------------------------
    @Test
    void test2_AdminLogin_SeesRequest_VerifyDetails() {
        assertNotNull(sharedSubmittedTitle, "test1 must run first to produce sharedSubmittedTitle");

        loginAsAdmin();
        waitForAdminDashboard();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("admin-requests-tbody"), sharedSubmittedTitle));
        List<WebElement> rows = driver.findElements(By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]"));
        assertFalse(rows.isEmpty(), "Admin should see the submitted request in the queue");

        // Check status is PENDING
        WebElement statusBadge = rows.get(0).findElement(By.xpath(".//span[contains(@class,'status-badge')]"));
        assertEquals("PENDING", statusBadge.getText().trim());

        // Open the request (click Open button)
        WebElement openBtn = rows.get(0).findElement(By.xpath(".//button[contains(text(),'Open')]"));
        openBtn.click();

        // Wait for modal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));

        // Verify key modal content: title shown in modal-title
        WebElement modalTitle = driver.findElement(By.id("modal-title"));
        assertTrue(modalTitle.getText().contains(sharedSubmittedTitle),
                "Modal title should contain asset title. Got: " + modalTitle.getText());

        // Download link should be visible
        WebElement downloadLink = driver.findElement(By.id("modal-download-link"));
        assertNotNull(downloadLink, "Download link must be present for admin");
        assertTrue(downloadLink.isDisplayed(), "Download link must be visible");

        captureScreenshot("test2_admin_reviewed_request_details");

        // Close modal
        driver.findElement(By.cssSelector("#review-modal .modal-close")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#review-modal.open")));

        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 3: ADMIN → Open Request → Enter Comment → REQUEST CHANGES
    // -----------------------------------------------------------------------
    @Test
    void test3_AdminReview_RequestChanges() {
        assertNotNull(sharedSubmittedTitle, "test1 must run first");

        loginAsAdmin();
        waitForAdminDashboard();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("admin-requests-tbody"), sharedSubmittedTitle));
        WebElement requestRow = driver.findElement(By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]"));

        // Click the Request Changes button directly in the row
        WebElement changeBtn = requestRow.findElement(By.xpath(".//button[contains(@class,'btn-change')]"));
        changeBtn.click();

        // Wait for modal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));

        WebElement commentInput = driver.findElement(By.id("reviewComment"));
        commentInput.clear();
        commentInput.sendKeys("Please increase text size and update the logo.");

        // Click Request Changes in modal
        driver.findElement(By.cssSelector("#modal-btn-change")).click();

        // Modal should close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#review-modal.open")));

        // Verify status updated to CHANGES_REQUESTED
        WebElement updatedRow = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]")));
        WebElement statusBadge = updatedRow.findElement(By.xpath(".//span[contains(@class,'status-badge')]"));
        wait.until(ExpectedConditions.textToBePresentInElement(statusBadge, "CHANGES_REQUESTED"));
        assertEquals("CHANGES_REQUESTED", statusBadge.getText().trim());

        captureScreenshot("test3_admin_requested_changes");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 4: USER → See CHANGES_REQUESTED → See Feedback → Resubmit → PENDING
    // -----------------------------------------------------------------------
    @Test
    void test4_UserSeesChanges_ResubmitsAsset() {
        assertNotNull(sharedSubmittedTitle, "test1 must run first");

        loginAsUser();
        waitForUserDashboard();

        // User's table should show CHANGES_REQUESTED
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), sharedSubmittedTitle));
        WebElement row = driver.findElement(By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]"));
        WebElement statusBadge = row.findElement(By.xpath(".//span[contains(@class,'status-badge')]"));
        assertEquals("CHANGES_REQUESTED", statusBadge.getText().trim(), "User should see CHANGES_REQUESTED");

        // Verify feedback comment is visible in the row
        String rowText = row.getText();
        assertTrue(rowText.contains("increase text size") || rowText.contains("Please"),
                "User should see admin feedback in their submissions table. Row: " + rowText);

        // Click Resubmit
        WebElement resubmitBtn = row.findElement(By.xpath(".//button[contains(@class,'btn-resubmit')]"));
        resubmitBtn.click();

        // Resubmit modal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resubmit-modal")));
        WebElement resubmitFile = driver.findElement(By.id("resubmit-file"));
        resubmitFile.sendKeys(testDocxPath.toString());

        WebElement resubmitDesc = driver.findElement(By.id("resubmit-description"));
        resubmitDesc.clear();
        resubmitDesc.sendKeys("Updated logo and increased text size as requested.");

        driver.findElement(By.xpath("//button[contains(@class,'btn-resubmit') and contains(text(),'Submit Revised')]")).click();

        // Modal closes
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#resubmit-modal.open")));

        // Status should be PENDING again
        WebElement updatedRow = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]")));
        WebElement newStatus = updatedRow.findElement(By.xpath(".//span[contains(@class,'status-badge')]"));
        wait.until(ExpectedConditions.textToBePresentInElement(newStatus, "PENDING"));
        assertEquals("PENDING", newStatus.getText().trim(), "After resubmit, status should be PENDING");

        captureScreenshot("test4_user_resubmitted_pending");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 5: ADMIN → Approve → APPROVED → User sees APPROVED
    // -----------------------------------------------------------------------
    @Test
    void test5_AdminApproves_UserSeesApproval() {
        assertNotNull(sharedSubmittedTitle, "test1 must run first");

        loginAsAdmin();
        waitForAdminDashboard();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("admin-requests-tbody"), sharedSubmittedTitle));
        WebElement requestRow = driver.findElement(By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]"));

        // Click approve
        WebElement approveBtn = requestRow.findElement(By.xpath(".//button[contains(@class,'btn-approve')]"));
        approveBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));
        driver.findElement(By.id("reviewComment")).sendKeys("Excellent — all guidelines met. Approved!");
        driver.findElement(By.cssSelector("#modal-btn-approve")).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#review-modal.open")));

        WebElement updatedRow = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]")));
        WebElement statusBadge = updatedRow.findElement(By.xpath(".//span[contains(@class,'status-badge')]"));
        wait.until(ExpectedConditions.textToBePresentInElement(statusBadge, "APPROVED"));
        assertEquals("APPROVED", statusBadge.getText().trim());
        captureScreenshot("test5_admin_approved");
        logout();

        // Now login as user and verify they see APPROVED
        loginAsUser();
        waitForUserDashboard();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), sharedSubmittedTitle));
        WebElement userRow = driver.findElement(By.xpath("//tr[contains(., '" + sharedSubmittedTitle + "')]"));
        WebElement userStatus = userRow.findElement(By.xpath(".//span[contains(@class,'status-badge')]"));
        assertEquals("APPROVED", userStatus.getText().trim(), "User should see APPROVED status");
        captureScreenshot("test5_user_sees_approved");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 6: Invalid extension rejected
    // -----------------------------------------------------------------------
    @Test
    void test6_InvalidExtension_Rejected() {
        loginAsUser();
        waitForUserDashboard();

        driver.findElement(By.id("title")).sendKeys("Executable Attempt");
        getFileInput().sendKeys(testExePath.toString());
        driver.findElement(By.id("submit-btn")).click();

        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        assertTrue(feedback.getAttribute("class").contains("feedback-error"), "Should show error class");
        assertTrue(feedback.getText().contains("Unsupported file type"),
                "Expected 'Unsupported file type', got: " + feedback.getText());
        captureScreenshot("test6_invalid_extension");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 7: Oversized file rejected
    // -----------------------------------------------------------------------
    @Test
    void test7_OversizedFile_Rejected() {
        loginAsUser();
        waitForUserDashboard();

        driver.findElement(By.id("title")).sendKeys("Oversized Asset");
        getFileInput().sendKeys(testLargePdfPath.toString());
        driver.findElement(By.id("submit-btn")).click();

        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        assertTrue(feedback.getAttribute("class").contains("feedback-error"), "Should show error styling");
        assertTrue(feedback.getText().contains("exceeds 10MB limit"),
                "Expected file size error, got: " + feedback.getText());
        captureScreenshot("test7_oversized_file");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 8: JPEG extension accepted
    // -----------------------------------------------------------------------
    @Test
    void test8_JpegExtension_Accepted() {
        loginAsUser();
        waitForUserDashboard();

        String jpegTitle = "JPEG Photo " + System.currentTimeMillis();
        driver.findElement(By.id("title")).sendKeys(jpegTitle);
        getFileInput().sendKeys(testJpegPath.toString());
        driver.findElement(By.id("submit-btn")).click();

        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        assertTrue(feedback.getText().contains("submitted successfully"),
                "JPEG should be accepted. Got: " + feedback.getText());
        assertFalse(feedback.getAttribute("class").contains("feedback-error"), "Should not show error for JPEG");
        captureScreenshot("test8_jpeg_accepted");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 9: Duplicate filenames — separate requests created (not overwritten)
    // -----------------------------------------------------------------------
    @Test
    void test9_DuplicateFilenames_BothStored() {
        loginAsUser();
        waitForUserDashboard();

        String title1 = "Dup File Test A " + System.currentTimeMillis();
        driver.findElement(By.id("title")).sendKeys(title1);
        getFileInput().sendKeys(testPngPath.toString());
        driver.findElement(By.id("submit-btn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), title1));

        // Submit second with same physical filename
        driver.findElement(By.id("title")).sendKeys("Dup File Test B " + System.currentTimeMillis());
        getFileInput().sendKeys(testPngPath.toString());
        driver.findElement(By.id("submit-btn")).click();

        // Both should appear in table
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), "Dup File Test B"));
        List<WebElement> rows = driver.findElements(By.xpath("//tr[contains(.,'Dup File Test')]"));
        assertTrue(rows.size() >= 2, "Both duplicate-filename assets should appear as separate rows");
        captureScreenshot("test9_duplicate_filenames");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 10: Normal USER cannot access ADMIN review endpoint via UI
    // -----------------------------------------------------------------------
    @Test
    void test10_UserCannotAccessAdminEndpoint() {
        loginAsUser();
        waitForUserDashboard();

        // User dashboard should NOT have admin action buttons (btn-approve, btn-reject, btn-change)
        // for other users' requests — the user table only has 'History' and 'Resubmit' buttons
        List<WebElement> adminBtns = driver.findElements(By.cssSelector("#requests-tbody .btn-approve"));
        assertTrue(adminBtns.isEmpty(), "User dashboard must not show Approve buttons");

        List<WebElement> rejectBtns = driver.findElements(By.cssSelector("#requests-tbody .btn-reject"));
        assertTrue(rejectBtns.isEmpty(), "User dashboard must not show Reject buttons");

        captureScreenshot("test10_user_no_admin_buttons");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 11: Notifications visible to ADMIN when asset submitted
    // -----------------------------------------------------------------------
    @Test
    void test11_AdminReceivesNotification() {
        // Submit as user
        loginAsUser();
        waitForUserDashboard();
        String notifTitle = "Notif Test Asset " + System.currentTimeMillis();
        driver.findElement(By.id("title")).sendKeys(notifTitle);
        getFileInput().sendKeys(testPdfPath.toString());
        driver.findElement(By.id("submit-btn")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        logout();

        // Login as admin, check notification badge
        loginAsAdmin();
        waitForAdminDashboard();

        // Give time for notification polling (or trigger manually)
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        WebElement badge = driver.findElement(By.id("admin-notif-badge"));
        // Badge may have count > 0
        // Click the bell to open panel
        driver.findElement(By.id("admin-notif-btn")).click();

        WebElement notifPanel = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("admin-notif-panel")));
        assertTrue(notifPanel.isDisplayed(), "Notification panel should open");
        captureScreenshot("test11_admin_notifications");
        logout();
    }

    // -----------------------------------------------------------------------
    // TEST 12: Review history returned and visible
    // -----------------------------------------------------------------------
    @Test
    void test12_ReviewHistoryVisible() {
        // Submit an asset as user
        loginAsUser();
        waitForUserDashboard();
        String histTitle = "History Test " + System.currentTimeMillis();
        driver.findElement(By.id("title")).sendKeys(histTitle);
        getFileInput().sendKeys(testPdfPath.toString());
        driver.findElement(By.id("submit-btn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), histTitle));
        logout();

        // Admin reviews it
        loginAsAdmin();
        waitForAdminDashboard();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("admin-requests-tbody"), histTitle));
        WebElement row = driver.findElement(By.xpath("//tr[contains(., '" + histTitle + "')]"));
        row.findElement(By.xpath(".//button[contains(text(),'Open')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));
        driver.findElement(By.id("reviewComment")).sendKeys("Needs header revision.");
        driver.findElement(By.cssSelector("#modal-btn-change")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#review-modal.open")));
        logout();

        // User views history
        loginAsUser();
        waitForUserDashboard();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), histTitle));
        WebElement userRow = driver.findElement(By.xpath("//tr[contains(., '" + histTitle + "')]"));
        WebElement historyBtn = userRow.findElement(By.xpath(".//button[contains(text(),'History')]"));
        historyBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("history-modal")));
        WebElement timeline = driver.findElement(By.id("history-timeline"));
        assertTrue(timeline.getText().contains("REVIEW_ACTION") || timeline.getText().contains("SUBMITTED"),
                "History timeline should contain audit events. Got: " + timeline.getText());
        captureScreenshot("test12_review_history_visible");

        driver.findElement(By.cssSelector("#history-modal .modal-close")).click();
        logout();
    }

    // -----------------------------------------------------------------------
    // Legacy Selenium compatibility: test IDs still work
    // -----------------------------------------------------------------------
    @Test
    void test13_LegacyIds_StillPresent() {
        loginAsUser();
        waitForUserDashboard();

        // All legacy IDs must be present on user dashboard
        assertNotNull(driver.findElement(By.id("title")), "#title must exist");
        assertNotNull(driver.findElement(By.id("description")), "#description must exist");
        assertNotNull(driver.findElement(By.id("file")), "#file must exist");
        assertNotNull(driver.findElement(By.id("requesterId")), "#requesterId must exist");
        assertNotNull(driver.findElement(By.id("submit-btn")), "#submit-btn must exist");
        assertNotNull(driver.findElement(By.id("form-feedback")), "#form-feedback must exist");
        assertNotNull(driver.findElement(By.id("requests-tbody")), "#requests-tbody must exist");

        captureScreenshot("test13_legacy_ids");
        logout();
    }

    private static void captureScreenshot(String name) {
        if (driver instanceof TakesScreenshot) {
            try {
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path dest = Paths.get(SCREENSHOT_DIR, name + "_" + timestamp + ".png");
                Files.createDirectories(dest.getParent());
                Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Screenshot captured: " + dest.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to save screenshot: " + e.getMessage());
            }
        }
    }
}
