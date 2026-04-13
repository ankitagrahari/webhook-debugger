package org.backendbrilliance.processorservice.repository;

import org.backendbrilliance.processorservice.entity.WebhookRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface WebhookRequestRepository extends JpaRepository<WebhookRequest, UUID> {

    long countByEndpointId(UUID endpointId);

    @Query("""
        SELECT COUNT(r) FROM WebhookRequest r
        WHERE r.endpointId = :endpointId
        AND r.receivedAt >= :since
    """)
    long countByEndpointIdSince(
            @Param("endpointId") UUID endpointId,
            @Param("since") LocalDateTime since
    );

    @Modifying
    @Query("""
        DELETE FROM WebhookRequest r
        WHERE r.endpointId = :endpointId
        AND r.receivedAt < :cutoff
    """)
    int deleteOlderThan(
            @Param("endpointId") UUID endpointId,
            @Param("cutoff") LocalDateTime cutoff
    );
}