package org.backendbrilliance.uiservice.controller;

import org.backendbrilliance.uiservice.dtos.ReplayRequest;
import org.backendbrilliance.uiservice.dtos.ReplayResponse;
import org.backendbrilliance.uiservice.dtos.WebhookRequestResponse;
import org.backendbrilliance.uiservice.service.ReplayService;
import org.backendbrilliance.uiservice.service.SseEmitterRegistry;
import org.backendbrilliance.uiservice.service.WebhookRequestService;
import org.backendbrilliance.uiservice.service.helper.MapperForEntityToDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class RequestController {

    private final WebhookRequestService requestService;
    private final ReplayService replayService;
    private final SseEmitterRegistry emitterRegistry;
    private final TaskScheduler scheduler;
    private final MapperForEntityToDTO mapper;

    public RequestController(
            WebhookRequestService requestService,
            ReplayService replayService,
            SseEmitterRegistry emitterRegistry, TaskScheduler scheduler, MapperForEntityToDTO mapper) {
        this.requestService = requestService;
        this.replayService = replayService;
        this.emitterRegistry = emitterRegistry;
        this.scheduler = scheduler;
        this.mapper = mapper;
    }

    // GET history
    @GetMapping("/endpoints/{slug}/requests")
    public List<WebhookRequestResponse> list(@PathVariable String slug) {
        return requestService.getLatestRequests(slug)
                .stream().map(mapper::webhookEntityToDTO).toList();
    }

    // SSE stream
    @GetMapping(value = "/endpoints/{slug}/requests/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String slug) {
        var emitter = new SseEmitter(Long.MAX_VALUE);
        emitterRegistry.register(slug, emitter);

        // Send a keep-alive comment every 15s — prevents idle disconnect
        ScheduledFuture<?> ping = scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, Instant.now(), Duration.ofSeconds(15));

        emitter.onCompletion(() -> {
            emitterRegistry.remove(slug, emitter);
            ping.cancel(true);
        });
        emitter.onTimeout(() -> {
            emitterRegistry.remove(slug, emitter);
            ping.cancel(true);
        });
        emitter.onError((e) -> {
            emitterRegistry.remove(slug, emitter);
            ping.cancel(true);
        });

        return emitter;
    }

    // Replay
    @PostMapping("/requests/{id}/replay")
    public ResponseEntity<ReplayResponse> replay(
            @PathVariable UUID id,
            @RequestBody ReplayRequest req) {
        var result = replayService.replay(id, req.targetUrl());
        return ResponseEntity.ok(result);
    }
}
