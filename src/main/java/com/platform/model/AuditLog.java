package com.platform.model;

import java.time.LocalDateTime;

public class AuditLog {
    private String id;
    private String requestId;
    private String userId;
    private String event;
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(String id, String requestId, String userId, String event, LocalDateTime timestamp) {
        this.id = id;
        this.requestId = requestId;
        this.userId = userId;
        this.event = event;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
