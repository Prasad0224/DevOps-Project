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
import java.util.Optional;

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

    // JUnit 5 extension to automatically capture screenshot on test failure
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
        // Base URL configurable via system property or environment variable
        baseUrl = System.getProperty("app.baseUrl");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = System.getenv("APP_BASE_URL");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        // Configure Chrome binary if installed in standard Windows 64-bit or 32-bit location
        ChromeOptions options = new ChromeOptions();
        String explicitChromeBin = System.getProperty("webdriver.chrome.bin");
        if (explicitChromeBin != null && !explicitChromeBin.isEmpty()) {
            options.setBinary(explicitChromeBin);
        } else {
            String[] possiblePaths = {
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
            };
            for (String p : possiblePaths) {
                if (new File(p).exists()) {
                    options.setBinary(p);
                    break;
                }
            }
        }

        boolean headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Create screenshot directory
        new File(SCREENSHOT_DIR).mkdirs();

        // Create actual test files for Selenium upload testing
        Path testFilesDir = Paths.get("target", "selenium-test-files");
        try {
            Files.createDirectories(testFilesDir);
            testPngPath = testFilesDir.resolve("banner.png").toAbsolutePath();
            testPdfPath = testFilesDir.resolve("document.pdf").toAbsolutePath();
            testDocxPath = testFilesDir.resolve("bad-draft.docx").toAbsolutePath();
            testExePath = testFilesDir.resolve("dangerous_script.exe").toAbsolutePath();
            testLargePdfPath = testFilesDir.resolve("large_movie.pdf").toAbsolutePath();

            Files.write(testPngPath, "dummy png asset data".getBytes());
            Files.write(testPdfPath, "dummy pdf asset data".getBytes());
            Files.write(testDocxPath, "dummy docx asset data".getBytes());
            Files.write(testExePath, "dangerous executable payload".getBytes());
            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(testLargePdfPath.toFile(), "rw")) {
                raf.setLength(15 * 1024 * 1024); // 15MB
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare test upload files", e);
        }
    }

    private WebElement getFileInput() {
        List<WebElement> elements = driver.findElements(By.id("file"));
        if (!elements.isEmpty()) {
            return elements.get(0);
        }
        return driver.findElement(By.id("fileName"));
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void loadApp() {
        for (int i = 0; i < 5; i++) {
            try {
                driver.get(baseUrl);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
                return;
            } catch (Exception e) {
                if (i == 4) {
                    throw e;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Test
    void test1_AssetSubmissionSuccess() {
        String testTitle = "Q3 Campaign Hero Asset " + System.currentTimeMillis();
        
        WebElement titleInput = driver.findElement(By.id("title"));
        WebElement descInput = driver.findElement(By.id("description"));
        WebElement fileInput = getFileInput();
        WebElement reqInput = driver.findElement(By.id("requesterId"));
        WebElement submitBtn = driver.findElement(By.id("submit-btn"));

        titleInput.clear();
        titleInput.sendKeys(testTitle);
        descInput.clear();
        descInput.sendKeys("Hero graphic for product launch");
        fileInput.sendKeys(testPngPath.toString());
        reqInput.clear();
        reqInput.sendKeys("marketing-lead");

        submitBtn.click();

        // Wait for success feedback message
        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        assertTrue(feedback.getText().contains("submitted successfully"), 
                "Expected success message, but got: " + feedback.getText());

        // Verify request row is present in the table
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), testTitle));
        List<WebElement> rows = driver.findElements(By.xpath("//tr[contains(., '" + testTitle + "')]"));
        assertFalse(rows.isEmpty(), "Submitted asset should appear in approval queue table");

        // Verify initial status is PENDING
        WebElement statusBadge = rows.get(0).findElement(By.xpath(".//span[contains(@class, 'status-badge')]"));
        assertEquals("PENDING", statusBadge.getText().trim());
    }

    @Test
    void test2_AssetSubmissionValidation_UnsupportedExtension() {
        WebElement titleInput = driver.findElement(By.id("title"));
        WebElement fileInput = getFileInput();
        WebElement submitBtn = driver.findElement(By.id("submit-btn"));

        titleInput.clear();
        titleInput.sendKeys("Executable File Attempt");
        fileInput.sendKeys(testExePath.toString());

        submitBtn.click();

        // Wait for error feedback
        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        assertTrue(feedback.getAttribute("class").contains("feedback-error"), 
                "Feedback should indicate error styling");
        assertTrue(feedback.getText().contains("Unsupported file type"), 
                "Expected 'Unsupported file type' error, but got: " + feedback.getText());
    }

    @Test
    void test3_AssetSubmissionValidation_OversizedFile() {
        WebElement titleInput = driver.findElement(By.id("title"));
        WebElement fileInput = getFileInput();
        WebElement submitBtn = driver.findElement(By.id("submit-btn"));

        titleInput.clear();
        titleInput.sendKeys("Oversized Video Asset");
        fileInput.sendKeys(testLargePdfPath.toString());

        submitBtn.click();

        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form-feedback")));
        assertTrue(feedback.getAttribute("class").contains("feedback-error"));
        assertTrue(feedback.getText().contains("exceeds 10MB limit"),
                "Expected file size limit error message, but got: " + feedback.getText());
    }

    @Test
    void test4_ReviewerApprovalWorkflow() {
        String testTitle = "Approval Test Asset " + System.currentTimeMillis();

        // 1. Submit Asset
        driver.findElement(By.id("title")).sendKeys(testTitle);
        getFileInput().sendKeys(testPdfPath.toString());
        driver.findElement(By.id("submit-btn")).click();

        // 2. Wait for submission to appear in table
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), testTitle));
        WebElement requestRow = driver.findElement(By.xpath("//tr[contains(., '" + testTitle + "')]"));
        
        // 3. Click Approve button on the row
        WebElement approveBtn = requestRow.findElement(By.xpath(".//button[contains(@class, 'btn-approve')]"));
        approveBtn.click();

        // 4. Modal should open
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));
        WebElement commentInput = driver.findElement(By.id("reviewComment"));
        commentInput.clear();
        commentInput.sendKeys("All guidelines met. Approved.");

        // 5. Confirm review action
        driver.findElement(By.id("confirm-review-btn")).click();

        // 6. Wait for modal to disappear
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("review-modal")));

        // 7. Verify status updated to APPROVED
        WebElement updatedRow = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//tr[contains(., '" + testTitle + "')]")));
        WebElement statusBadge = updatedRow.findElement(By.xpath(".//span[contains(@class, 'status-badge')]"));
        wait.until(ExpectedConditions.textToBePresentInElement(statusBadge, "APPROVED"));
        assertEquals("APPROVED", statusBadge.getText().trim());
    }

    @Test
    void test5_ReviewerRejectionAndRequestChangesWorkflow() {
        // --- Part A: Reject Workflow ---
        String rejectTitle = "Reject Test Asset " + System.currentTimeMillis();
        driver.findElement(By.id("title")).sendKeys(rejectTitle);
        getFileInput().sendKeys(testDocxPath.toString());
        driver.findElement(By.id("submit-btn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), rejectTitle));
        WebElement rejectRow = driver.findElement(By.xpath("//tr[contains(., '" + rejectTitle + "')]"));
        rejectRow.findElement(By.xpath(".//button[contains(@class, 'btn-reject')]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));
        driver.findElement(By.id("reviewComment")).sendKeys("Unsatisfactory quality. Rejected.");
        driver.findElement(By.id("confirm-review-btn")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("review-modal")));

        WebElement updatedRejectRow = driver.findElement(By.xpath("//tr[contains(., '" + rejectTitle + "')]"));
        WebElement rejectStatus = updatedRejectRow.findElement(By.xpath(".//span[contains(@class, 'status-badge')]"));
        wait.until(ExpectedConditions.textToBePresentInElement(rejectStatus, "REJECTED"));
        assertEquals("REJECTED", rejectStatus.getText().trim());

        // --- Part B: Request Changes Workflow ---
        String reviseTitle = "Revision Test Asset " + System.currentTimeMillis();
        driver.findElement(By.id("title")).sendKeys(reviseTitle);
        getFileInput().sendKeys(testPngPath.toString());
        driver.findElement(By.id("submit-btn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("requests-tbody"), reviseTitle));
        WebElement reviseRow = driver.findElement(By.xpath("//tr[contains(., '" + reviseTitle + "')]"));
        reviseRow.findElement(By.xpath(".//button[contains(@class, 'btn-change')]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));
        driver.findElement(By.id("reviewComment")).sendKeys("Please increase font size in section 2.");
        driver.findElement(By.id("confirm-review-btn")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("review-modal")));

        WebElement updatedReviseRow = driver.findElement(By.xpath("//tr[contains(., '" + reviseTitle + "')]"));
        WebElement reviseStatus = updatedReviseRow.findElement(By.xpath(".//span[contains(@class, 'status-badge')]"));
        wait.until(ExpectedConditions.textToBePresentInElement(reviseStatus, "CHANGES_REQUESTED"));
        assertEquals("CHANGES_REQUESTED", reviseStatus.getText().trim());
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
