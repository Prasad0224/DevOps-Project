package com.platform.service;

import com.platform.model.AssetRequest;
import com.platform.model.Notification;
import com.platform.model.RequestStatus;
import com.platform.model.ReviewAction;
import com.platform.repository.AssetRequestRepository;
import com.platform.repository.AuditLogRepository;
import com.platform.repository.NotificationRepository;
import com.platform.repository.ReviewActionRepository;
import com.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssetRequestServiceTest {

    private AssetRequestService service;
    private AssetRequestRepository repository;
    private ReviewActionRepository reviewActionRepository;
    private AuditLogRepository auditLogRepository;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        repository = new AssetRequestRepository();
        reviewActionRepository = new ReviewActionRepository();
        auditLogRepository = new AuditLogRepository();
        notificationRepository = new NotificationRepository();
        notificationService = new NotificationService(notificationRepository);
        userRepository = new UserRepository();
        // Seed a test admin so notifyAllAdmins doesn't NPE
        com.platform.model.User adminUser = new com.platform.model.User(
                "admin-test-id", "admin", "admin123", "Admin User", com.platform.model.Role.ADMIN);
        userRepository.save(adminUser);
        service = new AssetRequestService(repository, reviewActionRepository, auditLogRepository,
                notificationService, userRepository);
    }

    // -----------------------------------------------------------------------
    // Submit via legacy (fileName + fileSize) method
    // -----------------------------------------------------------------------

    @Test
    void submitRequest_ValidSubmission_ReturnsPendingRequest() {
        AssetRequest result = service.submitRequest("Ad Campaign", "Summer Ad", "ad.jpg", 1024, "user1");

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Ad Campaign", result.getTitle());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void submitRequest_UnsupportedFileType_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.submitRequest("Ad Campaign", "Summer Ad", "ad.exe", 1024, "user1"));
    }

    @Test
    void submitRequest_OversizedFile_ThrowsException() {
        long largeSize = 11 * 1024 * 1024; // 11MB
        assertThrows(IllegalArgumentException.class, () ->
                service.submitRequest("Ad Campaign", "Summer Ad", "ad.jpg", largeSize, "user1"));
    }

    @Test
    void submitRequest_MissingTitle_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.submitRequest("", "Summer Ad", "ad.jpg", 1024, "user1"));
    }

    // -----------------------------------------------------------------------
    // JPEG support (new requirement)
    // -----------------------------------------------------------------------

    @Test
    void submitRequest_JpegExtension_AcceptedAsSupportedType() {
        AssetRequest result = service.submitRequest("Jpeg Asset", "JPEG test", "photo.jpeg", 1024, "user1");
        assertNotNull(result);
        assertEquals(RequestStatus.PENDING, result.getStatus());
    }

    // -----------------------------------------------------------------------
    // Review workflow
    // -----------------------------------------------------------------------

    @Test
    void reviewRequest_ValidAction_UpdatesStatusAndLogs() {
        AssetRequest req = service.submitRequest("Ad Campaign", "Summer Ad", "ad.jpg", 1024, "user1");

        AssetRequest updated = service.reviewRequest(req.getId(), "reviewer1", RequestStatus.APPROVED, "Looks good");

        assertEquals(RequestStatus.APPROVED, updated.getStatus());
        assertEquals("Looks good", updated.getLatestComment());
        assertEquals(1, reviewActionRepository.findByRequestId(req.getId()).size());
        assertEquals(RequestStatus.APPROVED, reviewActionRepository.findByRequestId(req.getId()).get(0).getAction());
        assertEquals(1, auditLogRepository.findByRequestId(req.getId()).size());
    }

    @Test
    void reviewRequest_RejectAction_UpdatesStatusAndLogs() {
        AssetRequest req = service.submitRequest("Brand Logo", "New Logo", "logo.png", 2048, "designer");
        AssetRequest rejected = service.reviewRequest(req.getId(), "reviewer2", RequestStatus.REJECTED,
                "Does not meet branding guidelines");

        assertEquals(RequestStatus.REJECTED, rejected.getStatus());
        assertEquals(1, reviewActionRepository.findByRequestId(req.getId()).size());
        assertEquals(RequestStatus.REJECTED,
                reviewActionRepository.findByRequestId(req.getId()).get(0).getAction());
        assertEquals("Does not meet branding guidelines",
                reviewActionRepository.findByRequestId(req.getId()).get(0).getComment());
        assertEquals(1, auditLogRepository.findByRequestId(req.getId()).size());
        assertTrue(auditLogRepository.findByRequestId(req.getId()).get(0).getEvent().contains("REJECTED"));
    }

    @Test
    void reviewRequest_RequestChangesAction_UpdatesStatusAndLogs() {
        AssetRequest req = service.submitRequest("Whitepaper", "Draft v1", "paper.pdf", 4096, "author");
        AssetRequest changed = service.reviewRequest(req.getId(), "reviewer3",
                RequestStatus.CHANGES_REQUESTED, "Please revise section 3");

        assertEquals(RequestStatus.CHANGES_REQUESTED, changed.getStatus());
        assertEquals("Please revise section 3", changed.getLatestComment());
        assertEquals(RequestStatus.CHANGES_REQUESTED,
                reviewActionRepository.findByRequestId(req.getId()).get(0).getAction());
        assertEquals("Please revise section 3",
                reviewActionRepository.findByRequestId(req.getId()).get(0).getComment());
    }

    // -----------------------------------------------------------------------
    // Multipart submission
    // -----------------------------------------------------------------------

    @Test
    void submitRequest_MultipartFile_Valid_StoresFileAndReturnsPending() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "campaign-hero.png", "image/png", "sample png binary content".getBytes());

        AssetRequest result = service.submitRequest("Campaign Hero", "Summer Launch", file, "designer-1");

        assertNotNull(result);
        assertEquals("Campaign Hero", result.getTitle());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        // Stored file should use UUID prefix; original name preserved
        assertEquals("campaign-hero.png", result.getOriginalFileName());
        assertTrue(result.getStoredFileName().endsWith("campaign-hero.png"));
        // Verify file exists on disk
        File storedFile = new File("uploads", result.getStoredFileName());
        assertTrue(storedFile.exists(), "Uploaded file should exist in uploads directory");
    }

    @Test
    void submitRequest_MultipartFile_UnsupportedType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "text/plain", "echo hello".getBytes());
        assertThrows(IllegalArgumentException.class, () ->
                service.submitRequest("Script File", "Bash script", file, "user-1"));
    }

    @Test
    void submitRequest_MultipartFile_Empty_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () ->
                service.submitRequest("Empty Doc", "Empty doc", file, "user-1"));
    }

    @Test
    void submitRequest_MultipartFile_Oversized_ThrowsException() {
        byte[] largeBytes = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeBytes);
        assertThrows(IllegalArgumentException.class, () ->
                service.submitRequest("Large Doc", "Large doc", file, "user-1"));
    }

    // -----------------------------------------------------------------------
    // Filename collision prevention
    // -----------------------------------------------------------------------

    @Test
    void submitRequest_SameOriginalFilename_DoesNotOverwrite() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "banner.png", "image/png", "version one content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "banner.png", "image/png", "version two content".getBytes());

        AssetRequest r1 = service.submitRequest("Banner v1", "First", file1, "user-a");
        AssetRequest r2 = service.submitRequest("Banner v2", "Second", file2, "user-b");

        // Stored names must differ
        assertNotEquals(r1.getStoredFileName(), r2.getStoredFileName(),
                "Two uploads with same name must get different stored filenames");

        // Both files must exist on disk
        assertTrue(new File("uploads", r1.getStoredFileName()).exists());
        assertTrue(new File("uploads", r2.getStoredFileName()).exists());
    }

    // -----------------------------------------------------------------------
    // Resubmission
    // -----------------------------------------------------------------------

    @Test
    void resubmitRequest_WhenChangesRequested_ResetsStatusToPending() {
        // Submit
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "first version".getBytes());
        AssetRequest req = service.submitRequest("Report", "Initial", file, "user1");

        // Review -> CHANGES_REQUESTED
        service.reviewRequest(req.getId(), "admin", RequestStatus.CHANGES_REQUESTED, "Fix headers");

        // Resubmit
        MockMultipartFile revised = new MockMultipartFile(
                "file", "doc-v2.pdf", "application/pdf", "revised version".getBytes());
        AssetRequest resubmitted = service.resubmitRequest(req.getId(), revised, "Fixed headers", "user1");

        assertEquals(RequestStatus.PENDING, resubmitted.getStatus());
        assertEquals(1, resubmitted.getResubmissionCount());
        assertEquals("doc-v2.pdf", resubmitted.getOriginalFileName());
    }

    @Test
    void resubmitRequest_WhenNotChangesRequested_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "content".getBytes());
        AssetRequest req = service.submitRequest("Report", "Initial", file, "user1");

        MockMultipartFile revised = new MockMultipartFile(
                "file", "doc2.pdf", "application/pdf", "revised".getBytes());
        assertThrows(IllegalArgumentException.class, () ->
                service.resubmitRequest(req.getId(), revised, "desc", "user1"));
    }

    @Test
    void resubmitRequest_ByNonOwner_ThrowsSecurityException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "content".getBytes());
        AssetRequest req = service.submitRequest("Report", "Initial", file, "user1");
        service.reviewRequest(req.getId(), "admin", RequestStatus.CHANGES_REQUESTED, "Fix it");

        MockMultipartFile revised = new MockMultipartFile(
                "file", "doc2.pdf", "application/pdf", "revised".getBytes());
        assertThrows(SecurityException.class, () ->
                service.resubmitRequest(req.getId(), revised, "desc", "other-user"));
    }

    // -----------------------------------------------------------------------
    // Notifications
    // -----------------------------------------------------------------------

    @Test
    void notifications_GeneratedForAdmin_WhenAssetSubmitted() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "banner.png", "image/png", "content".getBytes());
        service.submitRequest("Test Asset", "Desc", file, "user1");

        // Admin should have received a notification
        List<Notification> adminNotifs = notificationService.getNotificationsForUser("admin-test-id");
        assertFalse(adminNotifs.isEmpty(), "Admin should receive notification when asset is submitted");
        assertTrue(adminNotifs.get(0).getMessage().contains("Test Asset"));
    }

    @Test
    void notifications_GeneratedForUser_WhenAssetReviewed() {
        // Seed the user in userRepository so notifyUser resolves the correct userId
        com.platform.model.User u = new com.platform.model.User(
                "user1-id", "user1", "pass", "User One", com.platform.model.Role.USER);
        userRepository.save(u);

        AssetRequest req = service.submitRequest("My Doc", "Desc", "doc.pdf", 1024, "user1");
        service.reviewRequest(req.getId(), "admin", RequestStatus.APPROVED, "Great work!");

        // user1's notifications should be stored under their userId "user1-id"
        List<Notification> userNotifs = notificationService.getNotificationsForUser("user1-id");
        assertFalse(userNotifs.isEmpty(), "User should receive notification when their asset is reviewed");
        assertTrue(userNotifs.get(0).getMessage().contains("APPROVED"));
    }

    // -----------------------------------------------------------------------
    // Review history
    // -----------------------------------------------------------------------

    @Test
    void getReviewHistory_ReturnsAllReviews() {
        AssetRequest req = service.submitRequest("Doc", "desc", "doc.pdf", 1024, "user1");
        service.reviewRequest(req.getId(), "admin", RequestStatus.CHANGES_REQUESTED, "Fix it");
        service.reviewRequest(req.getId(), "admin", RequestStatus.APPROVED, "Done");

        List<ReviewAction> history = service.getReviewHistory(req.getId());
        assertEquals(2, history.size());
    }
}
