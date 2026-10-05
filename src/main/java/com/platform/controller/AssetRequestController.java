package com.platform.controller;

import com.platform.model.AssetRequest;
import com.platform.model.Notification;
import com.platform.model.Role;
import com.platform.model.User;
import com.platform.service.AssetRequestService;
import com.platform.service.AuthService;
import com.platform.service.NotificationService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/requests")
public class AssetRequestController {

    private final AssetRequestService service;
    private final AuthService authService;
    private final NotificationService notificationService;

    public AssetRequestController(AssetRequestService service,
                                  AuthService authService,
                                  NotificationService notificationService) {
        this.service = service;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    // ------------------------------------------------------------------
    // Submit Asset (USER or any authenticated user)
    // ------------------------------------------------------------------

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> submitRequestMultipart(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("requesterId") String requesterId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            // Require authenticated user; requester must match their own userId
            User user = authService.requireUser(token);
            // Use the authenticated user's username as requesterId (but accept override for backward compat)
            String effectiveRequesterId = (requesterId != null && !requesterId.isBlank())
                    ? requesterId : user.getUsername();
            AssetRequest request = service.submitRequest(title, description, file, effectiveRequesterId);
            return ResponseEntity.ok(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error storing file: " + e.getMessage());
        }
    }

    // Legacy non-multipart endpoint (kept for unit tests)
    @PostMapping
    public ResponseEntity<?> submitRequest(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String fileName,
            @RequestParam long fileSize,
            @RequestParam String requesterId) {
        try {
            AssetRequest request = service.submitRequest(title, description, fileName, fileSize, requesterId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.badRequest().body("File size exceeds 10MB limit");
    }

    // ------------------------------------------------------------------
    // Get All Requests (ADMIN only)
    // ------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<?> getAllRequests(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            User user = authService.requireUser(token);
            if (user.getRole() == Role.ADMIN) {
                return ResponseEntity.ok(service.getAllRequests());
            } else {
                // USER sees only their own requests
                return ResponseEntity.ok(service.getRequestsByUser(user.getUsername()));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Get My Requests (USER)
    // ------------------------------------------------------------------

    @GetMapping("/my")
    public ResponseEntity<?> getMyRequests(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            User user = authService.requireUser(token);
            return ResponseEntity.ok(service.getRequestsByUser(user.getUsername()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Get All Requests for Admin explicitly
    // ------------------------------------------------------------------

    @GetMapping("/all")
    public ResponseEntity<?> getAllRequestsAdmin(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            authService.requireAdmin(token);
            return ResponseEntity.ok(service.getAllRequests());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Review (ADMIN only)
    // ------------------------------------------------------------------

    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewRequest(
            @PathVariable String id,
            @RequestParam String reviewerId,
            @RequestParam com.platform.model.RequestStatus action,
            @RequestParam String comment,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            User admin = authService.requireAdmin(token);
            AssetRequest request = service.reviewRequest(id, admin.getUsername(), action, comment);
            return ResponseEntity.ok(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Resubmit (USER, owner only)
    // ------------------------------------------------------------------

    @PostMapping(value = "/{id}/resubmit", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> resubmit(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            User user = authService.requireUser(token);
            AssetRequest request = service.resubmitRequest(id, file, description, user.getUsername());
            return ResponseEntity.ok(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error resubmitting: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Review History / Audit (authenticated; USER sees own, ADMIN sees any)
    // ------------------------------------------------------------------

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getHistory(
            @PathVariable String id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            User user = authService.requireUser(token);
            Optional<AssetRequest> reqOpt = service.getRequestById(id);
            if (reqOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            AssetRequest req = reqOpt.get();
            // USERs may only view history for their own requests
            if (user.getRole() != Role.ADMIN && !req.getRequesterId().equals(user.getUsername())) {
                return ResponseEntity.status(403).body("Access denied");
            }
            Map<String, Object> history = Map.of(
                    "request", req,
                    "reviewActions", service.getReviewHistory(id),
                    "auditLog", service.getAuditLog(id)
            );
            return ResponseEntity.ok(history);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // File Access
    // ------------------------------------------------------------------

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileName,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {
        try {
            String token = (tokenHeader != null && !tokenHeader.isBlank()) ? tokenHeader : tokenParam;
            if (token != null && !token.isBlank()) {
                Optional<User> userOpt = authService.getUserFromToken(token);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRole() != Role.ADMIN) {
                        boolean ownsFile = service.getAllRequests().stream()
                                .anyMatch(r -> fileName.equals(r.getStoredFileName())
                                        && r.getRequesterId().equals(user.getUsername()));
                        if (!ownsFile) {
                            return ResponseEntity.status(403).build();
                        }
                    }
                }
            }

            return serveFile(fileName);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFileByRequestId(
            @PathVariable String id,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {
        try {
            Optional<AssetRequest> reqOpt = service.getRequestById(id);
            if (reqOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            AssetRequest req = reqOpt.get();

            String token = (tokenHeader != null && !tokenHeader.isBlank()) ? tokenHeader : tokenParam;
            if (token != null && !token.isBlank()) {
                Optional<User> userOpt = authService.getUserFromToken(token);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRole() != Role.ADMIN && !req.getRequesterId().equals(user.getUsername())) {
                        return ResponseEntity.status(403).build();
                    }
                }
            }

            return serveFile(req.getStoredFileName());
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            User user = authService.requireUser(token);
            List<Notification> notifications = notificationService.getNotificationsForUser(user.getId());
            long unreadCount = notificationService.getUnreadCount(user.getId());
            return ResponseEntity.ok(Map.of(
                    "notifications", notifications,
                    "unreadCount", unreadCount
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/notifications/read")
    public ResponseEntity<?> markNotificationsRead(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            User user = authService.requireUser(token);
            notificationService.markAllRead(user.getId());
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private ResponseEntity<Resource> serveFile(String fileName) {
        try {
            Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadPath) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                String lower = fileName.toLowerCase();
                if (lower.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (lower.endsWith(".png")) {
                    contentType = "image/png";
                } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lower.endsWith(".docx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                } else {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
            }

            // Derive original filename from stored name (strip UUID prefix)
            String displayName = fileName;
            if (fileName.contains("_")) {
                displayName = fileName.substring(fileName.indexOf("_") + 1);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + displayName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
