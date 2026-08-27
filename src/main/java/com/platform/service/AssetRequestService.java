package com.platform.service;

import com.platform.model.AssetRequest;
import com.platform.model.AuditLog;
import com.platform.model.RequestStatus;
import com.platform.model.ReviewAction;
import com.platform.repository.AssetRequestRepository;
import com.platform.repository.AuditLogRepository;
import com.platform.repository.ReviewActionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AssetRequestService {

    private final AssetRequestRepository repository;
    private final ReviewActionRepository reviewActionRepository;
    private final AuditLogRepository auditLogRepository;
    
    // Limits hard-coded for MVP per review comments
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("jpg", "png", "pdf", "docx");

    public AssetRequestService(AssetRequestRepository repository,
                               ReviewActionRepository reviewActionRepository,
                               AuditLogRepository auditLogRepository) {
        this.repository = repository;
        this.reviewActionRepository = reviewActionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public AssetRequest submitRequest(String title, String description, String fileName, long fileSize, String requesterId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
        
        String extension = getFileExtension(fileName);
        if (!SUPPORTED_TYPES.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        AssetRequest request = new AssetRequest(
                UUID.randomUUID().toString(),
                title,
                description,
                fileName,
                requesterId,
                RequestStatus.PENDING,
                LocalDateTime.now()
        );

        return repository.save(request);
    }
    
    public AssetRequest reviewRequest(String requestId, String reviewerId, RequestStatus action, String comment) {
        AssetRequest request = repository.findAll().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
                
        request.setStatus(action);
        
        ReviewAction review = new ReviewAction(
                UUID.randomUUID().toString(),
                requestId,
                reviewerId,
                action,
                comment,
                LocalDateTime.now()
        );
        reviewActionRepository.save(review);
        
        AuditLog log = new AuditLog(
                UUID.randomUUID().toString(),
                requestId,
                reviewerId,
                "REVIEW_ACTION: " + action.name(),
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
        
        return repository.save(request);
    }

    public List<AssetRequest> getAllRequests() {
        return repository.findAll();
    }
    
    public List<ReviewAction> getReviewHistory(String requestId) {
        return reviewActionRepository.findByRequestId(requestId);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
