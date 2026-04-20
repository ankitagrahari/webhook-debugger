package org.backendbrilliance.uiservice.repository;

import org.backendbrilliance.uiservice.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    Optional<Endpoint> findBySlug(String slug);
    List<Endpoint> findAllByOrderByCreatedAtDesc();
}
