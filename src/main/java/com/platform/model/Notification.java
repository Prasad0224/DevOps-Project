package com.platform.model;

import java.time.LocalDateTime;

public class Notification {

    private String id;
    private String recipientUserId;
    private Role recipientRole;
    private String requestId;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;

    public Notification() {}

    public Notification(String id, String recipientUserId, Role recipientRole,
                        String requestId, String message, LocalDateTime timestamp) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.recipientRole = recipientRole;
        this.requestId = requestId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }

    public Role getRecipientRole() { return recipientRole; }
    public void setRecipientRole(Role recipientRole) { this.recipientRole = recipientRole; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
