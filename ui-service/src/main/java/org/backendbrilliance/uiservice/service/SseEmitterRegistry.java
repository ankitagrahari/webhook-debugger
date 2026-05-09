package org.backendbrilliance.uiservice.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void register(String slug, SseEmitter emitter) {
        emitters.computeIfAbsent(slug, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(String slug, SseEmitter emitter) {
        var list = emitters.get(slug);
        if (list != null) list.remove(emitter);
    }

    public void send(String slug, Object data) {
        var list = emitters.getOrDefault(slug, List.of());
        list.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(data, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
    }
}
