package com.platform.repository;

import com.platform.model.AssetRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AssetRequestRepository {

    private final Map<String, AssetRequest> storage = new ConcurrentHashMap<>();

    public AssetRequest save(AssetRequest request) {
        storage.put(request.getId(), request);
        return request;
    }

    public List<AssetRequest> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<AssetRequest> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<AssetRequest> findByRequesterId(String requesterId) {
        return storage.values().stream()
                .filter(r -> requesterId.equals(r.getRequesterId()))
                .collect(Collectors.toList());
    }
}
