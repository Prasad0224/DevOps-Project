package com.platform.service;

import com.platform.model.AssetRequest;
import com.platform.model.RequestStatus;
import com.platform.repository.AssetRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AssetRequestService {

    private final AssetRequestRepository repository;
    private final NotificationService notificationService;
    
    // Limits hard-coded for MVP per review comments
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("jpg", "png", "pdf", "docx");

    public AssetRequestService(AssetRequestRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
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
    
    public List<AssetRequest> getAllRequests() {
        return repository.findAll();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
