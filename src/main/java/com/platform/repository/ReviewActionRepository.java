package com.platform.repository;

import com.platform.model.ReviewAction;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ReviewActionRepository {
    private final Map<String, ReviewAction> storage = new ConcurrentHashMap<>();

    public ReviewAction save(ReviewAction action) {
        storage.put(action.getId(), action);
        return action;
    }

    public List<ReviewAction> findByRequestId(String requestId) {
        return storage.values().stream()
                .filter(action -> action.getRequestId().equals(requestId))
                .collect(Collectors.toList());
    }
}
