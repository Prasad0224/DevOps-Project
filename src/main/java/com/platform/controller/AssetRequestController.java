package com.platform.controller;

import com.platform.model.AssetRequest;
import com.platform.service.AssetRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class AssetRequestController {

    private final AssetRequestService service;

    public AssetRequestController(AssetRequestService service) {
        this.service = service;
    }

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

    @GetMapping
    public ResponseEntity<List<AssetRequest>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewRequest(
            @PathVariable String id,
            @RequestParam String reviewerId,
            @RequestParam com.platform.model.RequestStatus action,
            @RequestParam String comment) {
        try {
            AssetRequest request = service.reviewRequest(id, reviewerId, action, comment);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
