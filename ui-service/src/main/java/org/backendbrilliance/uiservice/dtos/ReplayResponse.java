package org.backendbrilliance.uiservice.dtos;

import java.util.List;
import java.util.Map;

public record ReplayResponse(
        int statusCode,
        String body,
        Map<String, List<String>> headers,
        Long durationMs) {

    public static ReplayResponse exception(String exception) {
        return new ReplayResponse(500, exception, null, null);
    }

    public String statusLabel() {
        return statusCode + " " + switch (statusCode / 100) {
            case 2 -> "OK";
            case 3 -> "Redirect";
            case 4 -> "Client Error";
            case 5 -> "Server Error";
            default -> "";
        };
    }
}