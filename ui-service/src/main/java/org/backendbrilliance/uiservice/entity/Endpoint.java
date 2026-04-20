package org.backendbrilliance.uiservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "endpoints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Endpoint {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String slug;

    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 100)
    private String label;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}