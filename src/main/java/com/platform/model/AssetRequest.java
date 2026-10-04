package com.platform.model;

import java.time.LocalDateTime;

public class AssetRequest {

    private String id;
    private String title;
    private String description;
    // storedFileName: the unique name on disk (UUID prefix + original name)
    private String storedFileName;
    // originalFileName: the original name shown in the UI
    private String originalFileName;
    // fileUrl kept for backward compatibility (same as storedFileName)
    private String fileUrl;
    private long fileSize;
    private String requesterId;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Populated from latest ReviewAction
    private String latestComment;
    private String latestReviewerId;

    private int resubmissionCount;

    public AssetRequest() {}

    // Legacy constructor (backward compat)
    public AssetRequest(String id, String title, String description, String fileUrl,
                        String requesterId, RequestStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fileUrl = fileUrl;
        this.storedFileName = fileUrl;
        this.originalFileName = fileUrl;
        this.requesterId = requesterId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.resubmissionCount = 0;
    }

    // Full constructor
    public AssetRequest(String id, String title, String description,
                        String storedFileName, String originalFileName, long fileSize,
                        String requesterId, RequestStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
        this.fileUrl = storedFileName; // backward compat
        this.fileSize = fileSize;
        this.requesterId = requesterId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.resubmissionCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
        this.fileUrl = storedFileName;
    }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
        this.storedFileName = fileUrl;
    }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getLatestComment() { return latestComment; }
    public void setLatestComment(String latestComment) { this.latestComment = latestComment; }

    public String getLatestReviewerId() { return latestReviewerId; }
    public void setLatestReviewerId(String latestReviewerId) { this.latestReviewerId = latestReviewerId; }

    public int getResubmissionCount() { return resubmissionCount; }
    public void setResubmissionCount(int resubmissionCount) { this.resubmissionCount = resubmissionCount; }
}
