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

    @BeforeEach
    void setUp() {
        repository = new AssetRequestRepository();
        reviewActionRepository = new com.platform.repository.ReviewActionRepository();
        auditLogRepository = new com.platform.repository.AuditLogRepository();
        service = new AssetRequestService(repository, reviewActionRepository, auditLogRepository);
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
}
