package com.platform.service;

import com.platform.model.Role;
import com.platform.model.User;
import com.platform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight session-based authentication service.
 * Sessions are stored in-memory. On restart, all sessions expire (acceptable for MVP).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    // token -> userId
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        seedUsers();
    }

    private void seedUsers() {
        User user = new User(UUID.randomUUID().toString(), "user", "user123", "Demo User", Role.USER);
        User admin = new User(UUID.randomUUID().toString(), "admin", "admin123", "Admin User", Role.ADMIN);
        userRepository.save(user);
        userRepository.save(admin);
    }

    /**
     * Authenticate with username/password. Returns session token on success.
     */
    public String login(String username, String password) {
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        User user = found.get();
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        return token;
    }

    /**
     * Invalidate a session token.
     */
    public void logout(String token) {
        sessions.remove(token);
    }

    /**
     * Resolve User from session token. Returns empty if token is invalid/expired.
     */
    public Optional<User> getUserFromToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        String userId = sessions.get(token);
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    /**
     * Require a valid authenticated user from the token header. Throws on failure.
     */
    public User requireUser(String token) {
        return getUserFromToken(token)
                .orElseThrow(() -> new SecurityException("Authentication required. Please log in."));
    }

    /**
     * Require the authenticated user to have ADMIN role. Throws on failure.
     */
    public User requireAdmin(String token) {
        User user = requireUser(token);
        if (user.getRole() != Role.ADMIN) {
            throw new SecurityException("Access denied. Admin privileges required.");
        }
        return user;
    }
}
