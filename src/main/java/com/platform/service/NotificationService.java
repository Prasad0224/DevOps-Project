package com.platform.service;

import com.platform.model.Notification;
import com.platform.model.Role;
import com.platform.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Legacy stub kept for backward compatibility with existing unit tests.
     * Also stores in-memory notifications.
     */
    public void notifyStatusChange(String requestId, String status) {
        System.out.println("Status change for request " + requestId + ": " + status);
    }

    /**
     * Notify a specific user (by userId) about an event on their asset.
     */
    public void notifyUser(String userId, String requestId, String message) {
        Notification n = new Notification(
                UUID.randomUUID().toString(),
                userId,
                Role.USER,
                requestId,
                message,
                LocalDateTime.now()
        );
        notificationRepository.save(n);
        System.out.println("[NOTIFY USER " + userId + "] " + message);
    }

    /**
     * Notify all admins about a new or changed asset.
     */
    public void notifyAdmin(String adminUserId, String requestId, String message) {
        Notification n = new Notification(
                UUID.randomUUID().toString(),
                adminUserId,
                Role.ADMIN,
                requestId,
                message,
                LocalDateTime.now()
        );
        notificationRepository.save(n);
        System.out.println("[NOTIFY ADMIN " + adminUserId + "] " + message);
    }

    /**
     * Get all notifications for a given user id.
     */
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByRecipientUserId(userId);
    }

    /**
     * Get unread notification count for a user.
     */
    public long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByRecipientUserId(userId);
    }

    /**
     * Mark all notifications as read for a user.
     */
    public void markAllRead(String userId) {
        notificationRepository.markAllReadForUser(userId);
    }
}
