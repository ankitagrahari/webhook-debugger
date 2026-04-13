package org.backendbrilliance.processorservice.repository;

import org.backendbrilliance.processorservice.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {
    Optional<Endpoint> findBySlug(String slug);
    boolean existsBySlug(String slug);
}