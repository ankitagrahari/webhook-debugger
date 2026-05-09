package org.backendbrilliance.uiservice.dtos;

public record UserResponse(
        String id,
        String email,
        String tier) {}