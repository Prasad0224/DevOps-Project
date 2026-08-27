package com.platform.repository;

import com.platform.model.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AuditLogRepository {
    private final Map<String, AuditLog> storage = new ConcurrentHashMap<>();

    public AuditLog save(AuditLog log) {
        storage.put(log.getId(), log);
        return log;
    }

    public List<AuditLog> findByRequestId(String requestId) {
        return storage.values().stream()
                .filter(log -> log.getRequestId().equals(requestId))
                .collect(Collectors.toList());
    }
}
