package org.backendbrilliance.captureservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.captureservice.producer.WebhookEventProducer;
import org.backendbrilliance.common.dto.WebhookEventDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookCaptureController {

    private final WebhookEventProducer producer;

    @RequestMapping(
            value = "/h/{slug}",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
            }
    )
    public Mono<ResponseEntity<Object>> capture(
            @PathVariable String slug,
            @RequestBody(required = false) String body,
            ServerHttpRequest request){

        log.debug("Incoming webhook: method: {} slug={}", request.getMethod(), slug);

        Map<String, String> headers = request.getHeaders().headerSet()
                .stream()
                .filter(e -> !e.getKey().startsWith(":"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(":", e.getValue())));

        Map<String, String> queryParams = request.getQueryParams()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(":", e.getValue())));

        WebhookEventDto event = WebhookEventDto.builder()
                .id(UUID.randomUUID())
                .endpointSlug(slug)
                .method(!Objects.isNull(request.getMethod()) ? request.getMethod().name(): "UNKNOWN")
                .headers(headers)
                .queryParams(queryParams)
                .body(body)
                .sourceIp(extractSourceIP(request))
                .contentType(request.getHeaders().getFirst("Content-Type"))
                .bodySizeBytes(!Objects.isNull(body) ? (long) body.getBytes().length : 0L)
                .receivedAt(LocalDateTime.now())
                .build();

        return producer.publish(event)
                .thenReturn(ResponseEntity.ok().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    private String extractSourceIP(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
