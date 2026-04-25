package org.backendbrilliance.uiservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.entity.Endpoint;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.exception.TierLimitException;
import org.backendbrilliance.uiservice.repository.EndpointRepository;
import org.backendbrilliance.uiservice.repository.UserRepository;
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
    private final UserRepository userRepository;

    public List<Endpoint> getAllEndpoints() {
        return endpointRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Endpoint> findBySlug(String slug) {
        return endpointRepository.findBySlug(slug);
    }

    @Transactional
    public Endpoint createEndpoint(String label, UUID userId) {

        if (userId != null) {
            Tier tier = userRepository.findById(userId)
                    .map(User::getTier)
                    .orElse(Tier.FREE);
            long existing = endpointRepository.countByUserId(userId);
            if (existing >= tier.maxEndpoints) {
                throw new TierLimitException(
                        "You've reached the " + tier.name() + " plan limit of "
                                + tier.maxEndpoints + " endpoint(s). Upgrade to create more.");
            }
        }
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
    public Endpoint createEndpoint(String label) {
        return createEndpoint(label, null);
    }

    @Transactional
    public void deleteEndpoint(UUID id) {
        endpointRepository.deleteById(id);
        log.info("Deleted endpoint [id={}]", id);
    }

    public Tier getTierForUser(UUID userId) {
        if (userId == null) return Tier.FREE;
        return userRepository.findById(userId)
                .map(User::getTier)
                .orElse(Tier.FREE);
    }

    private String generateSlug() {
        String slug;
        do {
            slug = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (endpointRepository.findBySlug(slug).isPresent());
        return slug;
    }
}
