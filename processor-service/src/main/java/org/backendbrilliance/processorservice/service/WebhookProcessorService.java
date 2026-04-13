package org.backendbrilliance.processorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.dto.WebhookEventDto;
import org.backendbrilliance.processorservice.entity.Endpoint;
import org.backendbrilliance.processorservice.entity.WebhookRequest;
import org.backendbrilliance.processorservice.repository.EndpointRepository;
import org.backendbrilliance.processorservice.repository.WebhookRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookProcessorService {

    private final WebhookRequestRepository requestRepository;
    private final EndpointRepository endpointRepository;

    @Transactional
    public void process(WebhookEventDto event) {
        Optional<Endpoint> endpoint = endpointRepository.findBySlug(event.getEndpointSlug());

        if (endpoint.isEmpty()) {
            log.warn("Received webhook for unknown slug [{}], discarding", event.getEndpointSlug());
            return;
        }

        if (isExpired(endpoint.get())) {
            log.warn("Received webhook for expired endpoint [slug={}], discarding", event.getEndpointSlug());
            return;
        }

        WebhookRequest request = WebhookRequest.builder()
                .id(event.getId())
                .endpointId(endpoint.get().getId())
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

    private boolean isExpired(Endpoint endpoint) {
        return endpoint.getExpiresAt() != null
                && endpoint.getExpiresAt().isBefore(java.time.LocalDateTime.now());
    }
}
