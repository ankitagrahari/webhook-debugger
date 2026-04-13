package org.backendbrilliance.captureservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.dto.WebhookEventDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventProducer {

    private final Sinks.Many<WebhookEventDto> webhookEventSink;

    /**
     * Emits a webhook event into the Sinks.Many buffer.
     * Binder picks it up and publishes to Kafka.
     * Zero Kafka awareness here — just push to a reactive sink.
     */
    public Mono<Void> publish(WebhookEventDto event) {
        return Mono.fromRunnable(() -> {
                    Sinks.EmitResult emitResult = webhookEventSink.tryEmitNext(event);
                    if(emitResult.isSuccess()){
                        log.debug("Emitted webhook event to sink [id={}, slug={}]", event.getId(), event.getEndpointSlug());
                    } else {
                        log.error("Failed to emit webhook event [id={}, slug={}, reason={}]", event.getId(), event.getEndpointSlug(), emitResult);
                        throw new RuntimeException(String.format("Failed to emit webhook event [%s], %s", event, emitResult));
                    }
                })
                .then();
    }
}
