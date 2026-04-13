package org.backendbrilliance.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventDto {

    private UUID id;
    private String endpointSlug;
    private String method;
    private Map<String, String> headers;
    private String body;
    private Map<String, String> queryParams;
    private String sourceIp;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime receivedAt;

    private String contentType;
    private Long bodySizeBytes;
}
