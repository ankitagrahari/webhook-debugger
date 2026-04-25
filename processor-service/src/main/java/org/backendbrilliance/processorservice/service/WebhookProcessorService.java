package org.backendbrilliance.processorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.dto.WebhookEventDto;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.processorservice.entity.Endpoint;
import org.backendbrilliance.processorservice.entity.User;
import org.backendbrilliance.processorservice.entity.WebhookRequest;
import org.backendbrilliance.processorservice.repository.EndpointRepository;
import org.backendbrilliance.processorservice.repository.UserRepository;
import org.backendbrilliance.processorservice.repository.WebhookRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookProcessorService {

    private final WebhookRequestRepository requestRepository;
    private final EndpointRepository endpointRepository;
    private final UserRepository userRepository;

    @Transactional
    public void process(WebhookEventDto event) {
        Optional<Endpoint> endpointOpt = endpointRepository.findBySlug(event.getEndpointSlug());

        if (endpointOpt.isEmpty()) {
            log.warn("Received webhook for unknown slug [{}], discarding", event.getEndpointSlug());
            return;
        }

        Endpoint endpoint = endpointOpt.get();
        if (isExpired(endpoint)) {
            log.warn("Received webhook for expired endpoint [slug={}], discarding", event.getEndpointSlug());
            return;
        }

        // Tier enforcement: daily request limit
        Tier tier = getTierForEndpoint(endpoint);
        if (tier.maxRequestsPerDay > 0) {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            long todayCount = requestRepository.countByEndpointIdSince(endpoint.getId(), startOfDay);
            if (todayCount >= tier.maxRequestsPerDay) {
                log.warn("Daily limit reached [slug={}, tier={}, limit={}], discarding",
                        event.getEndpointSlug(), tier, tier.maxRequestsPerDay);
                return;
            }
        }

        WebhookRequest request = WebhookRequest.builder()
                .id(event.getId())
                .endpointId(endpoint.getId())
                .method(event.getMethod())
                .headers(event.getHeaders())
                .body(event.getBody())
                .queryParams(event.getQueryParams())
                .sourceIp(event.getSourceIp())
                .contentType(event.getContentType())
                .bodySize(event.getBodySizeBytes())
                .receivedAt(event.getReceivedAt())
                .build();

        requestRepository.save(request);
        log.debug("Persisted webhook request [id={}, slug={}, method={}]",
                request.getId(), event.getEndpointSlug(), request.getMethod());
    }

    private Tier getTierForEndpoint(Endpoint endpoint) {
        if (endpoint.getUserId() == null) return Tier.FREE;
        return userRepository.findById(endpoint.getUserId())
                .map(User::getTier)
                .orElse(Tier.FREE);
    }

    private boolean isExpired(Endpoint endpoint) {
        return endpoint.getExpiresAt() != null
                && endpoint.getExpiresAt().isBefore(java.time.LocalDateTime.now());
    }
}
