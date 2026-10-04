package com.platform.controller;

import com.platform.model.User;
import com.platform.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        try {
            String token = authService.login(username, password);
            Optional<User> userOpt = authService.getUserFromToken(token);
            if (userOpt.isEmpty()) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Authentication failed"));
            }
            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "fullName", user.getFullName(),
                    "role", user.getRole().name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        Optional<User> userOpt = authService.getUserFromToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        if (token != null) {
            authService.logout(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
