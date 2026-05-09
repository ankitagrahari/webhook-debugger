package org.backendbrilliance.uiservice.entity;

public record RazorPayOrderConfig(
        String keyId,
        int amountPaise,
        String currency,
        String description,
        String email,
        String userId
) {}
