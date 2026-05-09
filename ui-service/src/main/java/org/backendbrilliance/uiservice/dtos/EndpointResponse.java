package org.backendbrilliance.uiservice.dtos;

import java.time.Instant;
import java.time.LocalDateTime;

public record EndpointResponse(
        String id,
        String slug,
        String label,
        String captureUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {}