package com.platform.service;

import com.platform.model.AssetRequest;
import com.platform.model.RequestStatus;
import com.platform.repository.AssetRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssetRequestServiceTest {

    private AssetRequestService service;
    private AssetRequestRepository repository;
    private com.platform.repository.ReviewActionRepository reviewActionRepository;
    private com.platform.repository.AuditLogRepository auditLogRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        repository = new AssetRequestRepository();
        reviewActionRepository = new com.platform.repository.ReviewActionRepository();
        auditLogRepository = new com.platform.repository.AuditLogRepository();
        notificationService = new NotificationService();
        service = new AssetRequestService(repository, reviewActionRepository, auditLogRepository, notificationService);
    }

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
        assertThrows(IllegalArgumentException.class, () -> {
            service.submitRequest("Ad Campaign", "Summer Ad", "ad.exe", 1024, "user1");
        });
    }

    @Test
    void submitRequest_OversizedFile_ThrowsException() {
        long largeSize = 11 * 1024 * 1024; // 11MB
        assertThrows(IllegalArgumentException.class, () -> {
            service.submitRequest("Ad Campaign", "Summer Ad", "ad.jpg", largeSize, "user1");
        });
    }

    @Test
    void submitRequest_MissingTitle_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.submitRequest("", "Summer Ad", "ad.jpg", 1024, "user1");
        });
    }

    @Test
    void reviewRequest_ValidAction_UpdatesStatusAndLogs() {
        AssetRequest req = service.submitRequest("Ad Campaign", "Summer Ad", "ad.jpg", 1024, "user1");
        
        AssetRequest updated = service.reviewRequest(req.getId(), "reviewer1", RequestStatus.APPROVED, "Looks good");
        
        assertEquals(RequestStatus.APPROVED, updated.getStatus());
        assertEquals(1, reviewActionRepository.findByRequestId(req.getId()).size());
        assertEquals(RequestStatus.APPROVED, reviewActionRepository.findByRequestId(req.getId()).get(0).getAction());
        assertEquals(1, auditLogRepository.findByRequestId(req.getId()).size());
    }

    @Test
    void reviewRequest_RejectAction_UpdatesStatusAndLogs() {
        AssetRequest req = service.submitRequest("Brand Logo", "New Logo", "logo.png", 2048, "designer");
        AssetRequest rejected = service.reviewRequest(req.getId(), "reviewer2", RequestStatus.REJECTED, "Does not meet branding guidelines");
        
        assertEquals(RequestStatus.REJECTED, rejected.getStatus());
        assertEquals(1, reviewActionRepository.findByRequestId(req.getId()).size());
        assertEquals(RequestStatus.REJECTED, reviewActionRepository.findByRequestId(req.getId()).get(0).getAction());
        assertEquals("Does not meet branding guidelines", reviewActionRepository.findByRequestId(req.getId()).get(0).getComment());
        assertEquals(1, auditLogRepository.findByRequestId(req.getId()).size());
        assertTrue(auditLogRepository.findByRequestId(req.getId()).get(0).getEvent().contains("REJECTED"));
    }

    @Test
    void reviewRequest_RequestChangesAction_UpdatesStatusAndLogs() {
        AssetRequest req = service.submitRequest("Whitepaper", "Draft v1", "paper.pdf", 4096, "author");
        AssetRequest changed = service.reviewRequest(req.getId(), "reviewer3", RequestStatus.CHANGES_REQUESTED, "Please revise section 3");
        
        assertEquals(RequestStatus.CHANGES_REQUESTED, changed.getStatus());
        assertEquals(RequestStatus.CHANGES_REQUESTED, reviewActionRepository.findByRequestId(req.getId()).get(0).getAction());
        assertEquals("Please revise section 3", reviewActionRepository.findByRequestId(req.getId()).get(0).getComment());
    }
}
