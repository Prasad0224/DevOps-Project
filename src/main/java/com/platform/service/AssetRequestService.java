package com.platform.service;

import com.platform.model.AssetRequest;
import com.platform.model.AuditLog;
import com.platform.model.RequestStatus;
import com.platform.model.ReviewAction;
import com.platform.model.User;
import com.platform.repository.AssetRequestRepository;
import com.platform.repository.AuditLogRepository;
import com.platform.repository.ReviewActionRepository;
import com.platform.repository.UserRepository;
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
import java.util.Optional;
import java.util.UUID;

@Service
public class AssetRequestService {

    private final AssetRequestRepository repository;
    private final ReviewActionRepository reviewActionRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    // jpeg added to fix prior omission; all required types present
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("jpg", "jpeg", "png", "pdf", "docx");
    private static final String UPLOAD_DIR = "uploads";

    public AssetRequestService(AssetRequestRepository repository,
                               ReviewActionRepository reviewActionRepository,
                               AuditLogRepository auditLogRepository,
                               NotificationService notificationService,
                               UserRepository userRepository) {
        this.repository = repository;
        this.reviewActionRepository = reviewActionRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // Submission (real multipart file)
    // -------------------------------------------------------------------------

    public AssetRequest submitRequest(String title, String description, MultipartFile file, String requesterId) {
        validateTitle(title);
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

        // Sanitise and create a unique stored name to prevent collisions
        String cleanOriginal = Paths.get(originalFilename).getFileName().toString();
        String storedName = UUID.randomUUID().toString() + "_" + cleanOriginal;

        Path uploadPath = resolveUploadPath();
        try {
            Path targetLocation = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
        }

        AssetRequest request = new AssetRequest(
                UUID.randomUUID().toString(),
                title,
                description,
                storedName,
                cleanOriginal,
                file.getSize(),
                requesterId,
                RequestStatus.PENDING,
                LocalDateTime.now()
        );

        AssetRequest saved = repository.save(request);
        notificationService.notifyStatusChange(saved.getId(), saved.getStatus().name());

        // Notify all admins about new submission
        notifyAllAdmins(saved.getId(), "New asset submitted: \"" + title + "\" by " + requesterId);

        addAuditLog(saved.getId(), requesterId, "SUBMITTED");
        return saved;
    }

    // -------------------------------------------------------------------------
    // Legacy submission (filename + fileSize only; used by existing unit tests)
    // -------------------------------------------------------------------------

    public AssetRequest submitRequest(String title, String description, String fileName, long fileSize, String requesterId) {
        validateTitle(title);

        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String extension = getFileExtension(fileName);
        if (!SUPPORTED_TYPES.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        String cleanName = Paths.get(fileName).getFileName().toString();

        AssetRequest request = new AssetRequest(
                UUID.randomUUID().toString(),
                title,
                description,
                cleanName,       // stored name (no prefix in test-only path)
                cleanName,       // original name
                fileSize,
                requesterId,
                RequestStatus.PENDING,
                LocalDateTime.now()
        );

        AssetRequest saved = repository.save(request);
        notificationService.notifyStatusChange(saved.getId(), saved.getStatus().name());
        return saved;
    }

    // -------------------------------------------------------------------------
    // Review
    // -------------------------------------------------------------------------

    public AssetRequest reviewRequest(String requestId, String reviewerId, RequestStatus action, String comment) {
        AssetRequest request = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        request.setStatus(action);
        request.setLatestComment(comment);
        request.setLatestReviewerId(reviewerId);
        request.setUpdatedAt(LocalDateTime.now());

        ReviewAction review = new ReviewAction(
                UUID.randomUUID().toString(),
                requestId,
                reviewerId,
                action,
                comment,
                LocalDateTime.now()
        );
        reviewActionRepository.save(review);

        addAuditLog(requestId, reviewerId, "REVIEW_ACTION: " + action.name() + " - " + comment);

        notificationService.notifyStatusChange(requestId, action.name());

        // Notify the asset owner by their real user ID (if found) or by requesterId string as fallback
        String ownerNotifyId = userRepository.findByUsername(request.getRequesterId())
                .map(u -> u.getId())
                .orElse(request.getRequesterId()); // fallback to username string for legacy/test usage
        notificationService.notifyUser(
                ownerNotifyId,
                requestId,
                "Your asset \"" + request.getTitle() + "\" was " + action.name()
                        + (comment != null && !comment.isBlank() ? ". Feedback: " + comment : "")
        );

        return repository.save(request);
    }

    // -------------------------------------------------------------------------
    // Resubmission
    // -------------------------------------------------------------------------

    public AssetRequest resubmitRequest(String requestId, MultipartFile file, String description, String requesterId) {
        AssetRequest request = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!request.getRequesterId().equals(requesterId)) {
            throw new SecurityException("Access denied. Only the owner may resubmit this request.");
        }

        if (request.getStatus() != RequestStatus.CHANGES_REQUESTED) {
            throw new IllegalArgumentException("Resubmission is only allowed when status is CHANGES_REQUESTED");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required for resubmission");
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

        String cleanOriginal = Paths.get(originalFilename).getFileName().toString();
        String storedName = UUID.randomUUID().toString() + "_" + cleanOriginal;

        Path uploadPath = resolveUploadPath();
        try {
            Path targetLocation = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store resubmitted file: " + e.getMessage(), e);
        }

        request.setStoredFileName(storedName);
        request.setOriginalFileName(cleanOriginal);
        request.setFileSize(file.getSize());
        if (description != null && !description.isBlank()) {
            request.setDescription(description);
        }
        request.setStatus(RequestStatus.PENDING);
        request.setLatestComment(null);
        request.setLatestReviewerId(null);
        request.setResubmissionCount(request.getResubmissionCount() + 1);
        request.setUpdatedAt(LocalDateTime.now());

        addAuditLog(requestId, requesterId, "RESUBMITTED: new file " + cleanOriginal);
        notificationService.notifyStatusChange(requestId, "PENDING");

        // Notify all admins
        notifyAllAdmins(requestId, "Asset resubmitted: \"" + request.getTitle() + "\" by " + requesterId);

        return repository.save(request);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public List<AssetRequest> getAllRequests() {
        return repository.findAll();
    }

    public List<AssetRequest> getRequestsByUser(String requesterId) {
        return repository.findByRequesterId(requesterId);
    }

    public Optional<AssetRequest> getRequestById(String id) {
        return repository.findById(id);
    }

    public List<ReviewAction> getReviewHistory(String requestId) {
        return reviewActionRepository.findByRequestId(requestId);
    }

    public List<AuditLog> getAuditLog(String requestId) {
        return auditLogRepository.findByRequestId(requestId);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title length must be under 100 characters");
        }
    }

    private Path resolveUploadPath() {
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create upload directory", e);
            }
        }
        return uploadPath;
    }

    private void addAuditLog(String requestId, String userId, String event) {
        AuditLog log = new AuditLog(
                UUID.randomUUID().toString(),
                requestId,
                userId,
                event,
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }

    private void notifyAllAdmins(String requestId, String message) {
        userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ADMIN"))
                .forEach(admin -> notificationService.notifyAdmin(admin.getId(), requestId, message));
    }

    String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
