package org.backendbrilliance.processorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.processorservice.entity.Endpoint;
import org.backendbrilliance.processorservice.entity.User;
import org.backendbrilliance.processorservice.repository.EndpointRepository;
import org.backendbrilliance.processorservice.repository.UserRepository;
import org.backendbrilliance.processorservice.repository.WebhookRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetentionCleanupService {

    private final EndpointRepository endpointRepository;
    private final UserRepository userRepository;
    private final WebhookRequestRepository requestRepository;

    /**
     * Runs every night at 02:00.
     * FREE = 1 day, PRO = 30 days, TEAM = 90 days retention.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredRequests() {
        log.info("Starting nightly retention cleanup");
        //Potential performance issue if a large amount data is fetched
        //TODO: Use paging
        List<Endpoint> endpoints = endpointRepository.findAll();
        int totalDeleted = 0;

        for (Endpoint endpoint : endpoints) {
            Tier tier = getTier(endpoint);
            LocalDateTime cutoff = LocalDateTime.now().minusDays(tier.historyDays);
            int deleted = requestRepository.deleteOlderThan(endpoint.getId(), cutoff);
            if (deleted > 0) {
                log.debug("Cleaned {} requests [slug={}, tier={}]",
                        deleted, endpoint.getSlug(), tier);
                totalDeleted += deleted;
            }
        }
        log.info("Retention cleanup done — {} requests deleted", totalDeleted);
    }

    private Tier getTier(Endpoint endpoint) {
        if (endpoint.getUserId() == null) return Tier.FREE;
        return userRepository.findById(endpoint.getUserId())
                .map(User::getTier)
                .orElse(Tier.FREE);
    }
}
