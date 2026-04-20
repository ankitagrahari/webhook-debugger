package org.backendbrilliance.uiservice.repository;

import org.backendbrilliance.uiservice.entity.WebhookRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookRepository extends JpaRepository<WebhookRequest, UUID> {
    List<WebhookRequest> findTop100ByEndpointIdOrderByReceivedAtDesc(UUID endpointId);
    long countByEndpointId(UUID endpointId);
    Page<WebhookRequest> findByEndpointIdOrderByReceivedAtDesc(UUID endpointId, Pageable pageable);
}
