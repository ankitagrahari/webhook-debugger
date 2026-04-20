package org.backendbrilliance.uiservice.service;

import lombok.RequiredArgsConstructor;
import org.backendbrilliance.uiservice.entity.WebhookRequest;
import org.backendbrilliance.uiservice.repository.WebhookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookRequestService {

    private final WebhookRepository requestRepository;

    public List<WebhookRequest> getLatestRequests(UUID endpointId) {
        return requestRepository.findTop100ByEndpointIdOrderByReceivedAtDesc(endpointId);
    }

    public long countRequests(UUID endpointId) {
        return requestRepository.countByEndpointId(endpointId);
    }

    public Optional<WebhookRequest> findById(UUID id) {
        return requestRepository.findById(id);
    }
}
