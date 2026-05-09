package org.backendbrilliance.uiservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.uiservice.dtos.ReplayResponse;
import org.backendbrilliance.uiservice.entity.WebhookRequest;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplayService {

    private final WebhookRequestService webhookRequestService;

    private static final Set<String> SKIP_HEADERS = Set.of(
            "host", "content-length", "transfer-encoding",
            "connection", "keep-alive", "proxy-authenticate",
            "proxy-authorization", "te", "trailers", "upgrade"
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public ReplayResult replay(WebhookRequest original, String targetUrl) {
        log.info("Replaying request [id={}, method={}, target={}]",
                original.getId(), original.getMethod(), targetUrl);
        try {
            URI uri = URI.create(targetUrl);
            HttpRequest.BodyPublisher bodyPublisher = original.getBody() != null
                    ? HttpRequest.BodyPublishers.ofString(original.getBody())
                    : HttpRequest.BodyPublishers.noBody();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(15))
                    .method(original.getMethod(), bodyPublisher);

            if (original.getHeaders() != null) {
                for (Map.Entry<String, String> entry : original.getHeaders().entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    if (!SKIP_HEADERS.contains(key)) {
                        try {
                            builder.header(entry.getKey(), entry.getValue());
                        } catch (IllegalArgumentException e) {
                            log.debug("Skipping header [{}]: {}", entry.getKey(), e.getMessage());
                        }
                    }
                }
            }

            HttpResponse<String> response = httpClient.send(
                    builder.build(), HttpResponse.BodyHandlers.ofString());
            log.info("Replay complete [id={}, status={}]", original.getId(), response.statusCode());
            return ReplayResult.success(response.statusCode(), response.body());

        } catch (Exception e) {
            log.error("Replay failed [id={}]: {}", original.getId(), e.getMessage());
            return ReplayResult.failure(e.getMessage());
        }
    }

    public ReplayResponse replay(UUID requestId, String targetUrl) {

        //Fetch WebhookRequest from DB based on the id.
        WebhookRequest original = webhookRequestService.findById(requestId).orElse(null);

        if(original == null) {
            return ReplayResponse.exception("no request found");
        }

        log.info("Replaying request [id={}, method={}, target={}]",
                original.getId(), original.getMethod(), targetUrl);
        try {
            long start = System.currentTimeMillis();
            URI uri = URI.create(targetUrl);
            HttpRequest.BodyPublisher bodyPublisher = original.getBody() != null
                    ? HttpRequest.BodyPublishers.ofString(original.getBody())
                    : HttpRequest.BodyPublishers.noBody();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(15))
                    .method(original.getMethod(), bodyPublisher);

            if (original.getHeaders() != null) {
                for (Map.Entry<String, String> entry : original.getHeaders().entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    if (!SKIP_HEADERS.contains(key)) {
                        try {
                            builder.header(entry.getKey(), entry.getValue());
                        } catch (IllegalArgumentException e) {
                            log.debug("Skipping header [{}]: {}", entry.getKey(), e.getMessage());
                        }
                    }
                }
            }

            HttpResponse<String> response = httpClient.send(
                    builder.build(), HttpResponse.BodyHandlers.ofString());
            long end = System.currentTimeMillis();
            log.info("Replay complete [id={}, status={}]", original.getId(), response.statusCode());
            return new ReplayResponse(response.statusCode(), response.body(), response.headers().map(), (end-start));

        } catch (Exception e) {
            log.error("Replay failed [id={}]: {}", original.getId(), e.getMessage());
            return ReplayResponse.exception(e.getMessage());
        }
    }

    public record ReplayResult(boolean success, int statusCode, String responseBody, String errorMessage) {
        public static ReplayResult success(int statusCode, String body) {
            return new ReplayResult(true, statusCode, body, null);
        }
        public static ReplayResult failure(String error) {
            return new ReplayResult(false, 0, null, error);
        }
        public String statusLabel() {
            if (!success) return "Error";
            return statusCode + " " + switch (statusCode / 100) {
                case 2 -> "OK";
                case 3 -> "Redirect";
                case 4 -> "Client Error";
                case 5 -> "Server Error";
                default -> "";
            };
        }
    }
}
