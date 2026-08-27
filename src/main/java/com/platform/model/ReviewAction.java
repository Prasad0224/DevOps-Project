package com.platform.model;

import java.time.LocalDateTime;

public class ReviewAction {
    private String id;
    private String requestId;
    private String reviewerId;
    private RequestStatus action;
    private String comment;
    private LocalDateTime actionDate;

    public ReviewAction() {}

    public ReviewAction(String id, String requestId, String reviewerId, RequestStatus action, String comment, LocalDateTime actionDate) {
        this.id = id;
        this.requestId = requestId;
        this.reviewerId = reviewerId;
        this.action = action;
        this.comment = comment;
        this.actionDate = actionDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public RequestStatus getAction() { return action; }
    public void setAction(RequestStatus action) { this.action = action; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
}
