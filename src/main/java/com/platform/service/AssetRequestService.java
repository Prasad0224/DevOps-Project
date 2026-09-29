package com.platform.service;

import com.platform.model.AssetRequest;
import com.platform.model.AuditLog;
import com.platform.model.RequestStatus;
import com.platform.model.ReviewAction;
import com.platform.repository.AssetRequestRepository;
import com.platform.repository.AuditLogRepository;
import com.platform.repository.ReviewActionRepository;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AssetRequestService {

    private final AssetRequestRepository repository;
    private final ReviewActionRepository reviewActionRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    
    // Limits hard-coded for MVP per review comments
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("jpg", "png", "pdf", "docx");
    private static final String UPLOAD_DIR = "uploads";

    public AssetRequestService(AssetRequestRepository repository,
                               ReviewActionRepository reviewActionRepository,
                               AuditLogRepository auditLogRepository,
                               NotificationService notificationService) {
        this.repository = repository;
        this.reviewActionRepository = reviewActionRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
    }

    public AssetRequest submitRequest(String title, String description, MultipartFile file, String requesterId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title length must be under 100 characters");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        String extension = getFileExtension(originalFilename);
        if (!SUPPORTED_TYPES.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        // Clean file name to prevent path traversal
        String cleanFileName = Paths.get(originalFilename).getFileName().toString();

        // Store uploaded file in a simple local uploads directory
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path targetLocation = uploadPath.resolve(cleanFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
        }

        AssetRequest request = new AssetRequest(
                UUID.randomUUID().toString(),
                title,
                description,
                cleanFileName,
                requesterId,
                RequestStatus.PENDING,
                LocalDateTime.now()
        );

        AssetRequest savedRequest = repository.save(request);
        notificationService.notifyStatusChange(savedRequest.getId(), savedRequest.getStatus().name());
        return savedRequest;
    }

    public AssetRequest submitRequest(String title, String description, String fileName, long fileSize, String requesterId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title length must be under 100 characters");
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

        AssetRequest savedRequest = repository.save(request);
        notificationService.notifyStatusChange(savedRequest.getId(), savedRequest.getStatus().name());
        return savedRequest;
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
        
        notificationService.notifyStatusChange(requestId, action.name());
        
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
