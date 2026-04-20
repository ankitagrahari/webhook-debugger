package org.backendbrilliance.uiservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.uiservice.entity.Endpoint;
import org.backendbrilliance.uiservice.repository.EndpointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointRepository endpointRepository;

    public List<Endpoint> getAllEndpoints() {
        return endpointRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Endpoint> findBySlug(String slug) {
        return endpointRepository.findBySlug(slug);
    }

    @Transactional
    public Endpoint createEndpoint(String label) {
        Endpoint endpoint = Endpoint.builder()
                .id(UUID.randomUUID())
                .slug(generateSlug())
                .label(label != null && !label.isBlank() ? label : null)
                .createdAt(LocalDateTime.now())
                .build();
        Endpoint saved = endpointRepository.save(endpoint);
        log.info("Created endpoint [slug={}, label={}]", saved.getSlug(), saved.getLabel());
        return saved;
    }

    @Transactional
    public void deleteEndpoint(UUID id) {
        endpointRepository.deleteById(id);
        log.info("Deleted endpoint [id={}]", id);
    }

    private String generateSlug() {
        String slug;
        do {
            slug = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (endpointRepository.findBySlug(slug).isPresent());
        return slug;
    }
}
