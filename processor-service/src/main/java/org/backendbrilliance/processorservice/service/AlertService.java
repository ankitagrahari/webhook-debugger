package org.backendbrilliance.processorservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.processorservice.entity.Endpoint;
import org.backendbrilliance.processorservice.entity.User;
import org.backendbrilliance.processorservice.repository.EndpointRepository;
import org.backendbrilliance.processorservice.repository.UserRepository;
import org.backendbrilliance.processorservice.repository.WebhookRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AlertService {

    private final EndpointRepository endpointRepository;
    private final UserRepository userRepository;
    private final WebhookRequestRepository requestRepository;
    private final RestTemplate restTemplate;

    @Value("${alerts.slack.webhook-url:}")
    private String slackWebhookUrl;

    @Value("${alerts.inactivity-threshold-hours:2}")
    private int inactivityThresholdHours;

    public AlertService(EndpointRepository endpointRepository,
                        UserRepository userRepository,
                        WebhookRequestRepository requestRepository) {
        this.endpointRepository = endpointRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Runs every hour.
     * For PRO/TEAM users — checks if any endpoint has gone silent
     * (no requests in the last N hours) and sends a Slack alert.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkInactiveEndpoints() {
        List<Endpoint> endpoints = endpointRepository.findAll();
        LocalDateTime threshold = LocalDateTime.now().minusHours(inactivityThresholdHours);

        for (Endpoint endpoint : endpoints) {
            if (endpoint.getUserId() == null) continue;

            User user = userRepository.findById(endpoint.getUserId()).orElse(null);
            if (user == null || user.getTier() == Tier.FREE) continue;

            long recentCount = requestRepository.countByEndpointIdSince(
                    endpoint.getId(), threshold);

            if (recentCount == 0) {
                String label = endpoint.getLabel() != null
                        ? endpoint.getLabel()
                        : endpoint.getSlug();
                sendSlackAlert(user.getEmail(), label, inactivityThresholdHours);
            }
        }
    }

    private void sendSlackAlert(String userEmail, String endpointLabel, int hours) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank()) {
            log.debug("Slack webhook URL not configured — skipping alert");
            return;
        }
        try {
            String message = String.format(
                    "⚠️ *HookSpy Alert*\nEndpoint *%s* has received no requests in the last %d hour(s).\nUser: %s",
                    endpointLabel, hours, userEmail);

            Map<String, String> payload = Map.of("text", message);
            restTemplate.postForEntity(slackWebhookUrl, payload, String.class);
            log.info("Sent inactivity alert for endpoint [{}]", endpointLabel);
        } catch (Exception e) {
            log.warn("Failed to send Slack alert: {}", e.getMessage());
        }
    }
}