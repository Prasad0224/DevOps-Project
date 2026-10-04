package com.platform.repository;

import com.platform.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class NotificationRepository {

    private final Map<String, Notification> storage = new ConcurrentHashMap<>();

    public Notification save(Notification notification) {
        storage.put(notification.getId(), notification);
        return notification;
    }

    public List<Notification> findByRecipientUserId(String userId) {
        return storage.values().stream()
                .filter(n -> userId.equals(n.getRecipientUserId()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    public long countUnreadByRecipientUserId(String userId) {
        return storage.values().stream()
                .filter(n -> userId.equals(n.getRecipientUserId()) && !n.isRead())
                .count();
    }

    public void markAllReadForUser(String userId) {
        storage.values().stream()
                .filter(n -> userId.equals(n.getRecipientUserId()))
                .forEach(n -> n.setRead(true));
    }
}
