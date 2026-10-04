package com.platform.repository;

import com.platform.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<String, User> byId = new ConcurrentHashMap<>();
    private final Map<String, User> byUsername = new ConcurrentHashMap<>();

    public User save(User user) {
        byId.put(user.getId(), user);
        byUsername.put(user.getUsername().toLowerCase(), user);
        return user;
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username == null ? "" : username.toLowerCase()));
    }

    public java.util.List<User> findAll() {
        return new java.util.ArrayList<>(byId.values());
    }
}
