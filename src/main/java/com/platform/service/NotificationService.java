package com.platform.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void notifyStatusChange(String requestId, String status) {
        System.out.println("Email sent for request " + requestId + ": Status changed to " + status);
    }
}
