package org.backendbrilliance.uiservice.dtos;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

public record WebhookRequestResponse(
        String id,
        String method,
        String sourceIp,
        String contentType,
        long bodySize,
        Map<String, String> headers,
        String body,
        Map<String, String> queryParams,
        LocalDateTime receivedAt) {
}