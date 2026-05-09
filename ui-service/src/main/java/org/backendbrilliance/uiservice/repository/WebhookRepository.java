package org.backendbrilliance.uiservice.repository;

import org.backendbrilliance.uiservice.entity.WebhookRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookRepository extends JpaRepository<WebhookRequest, UUID> {
    List<WebhookRequest> findTop100ByEndpointIdOrderByReceivedAtDesc(UUID endpointId);
    long countByEndpointId(UUID endpointId);
    Page<WebhookRequest> findByEndpointIdOrderByReceivedAtDesc(UUID endpointId, Pageable pageable);

    @Query("""
        SELECT r FROM WebhookRequest r
        WHERE r.endpointId = :endpointId
        AND r.receivedAt >= :since
        ORDER BY r.receivedAt DESC
        LIMIT 100
    """)
    List<WebhookRequest> findRecentByEndpointId(
            @Param("endpointId") UUID endpointId,
            @Param("since") LocalDateTime since
    );

    @Query("""
        SELECT r FROM WebhookRequest r JOIN Endpoint e
            on r.endpointId = e.id
        WHERE e.slug = :slug
        ORDER BY r.receivedAt DESC
        LIMIT 100
    """)
    List<WebhookRequest> findRecentBySlug(
            @Param("slug") String slug
    );


}
