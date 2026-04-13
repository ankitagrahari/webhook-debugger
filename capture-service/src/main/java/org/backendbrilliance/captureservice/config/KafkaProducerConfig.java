package org.backendbrilliance.captureservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.backendbrilliance.common.dto.WebhookEventDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration(proxyBeanMethods = false)
public class KafkaProducerConfig {

    /**
     * Sinks.Many is a thread-safe reactive in-memory buffer.
     * Controller pushes events here.
     * Binder drains this as a Flux into Kafka automatically.
     *
     * unicast().onBackpressureBuffer() = one subscriber (the binder),
     * buffer events if Kafka is slow rather than dropping them.
     */
    @Bean
    public Sinks.Many<WebhookEventDto>  webhookEventSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

    /**
     * Binding name convention: {beanName}-out-0
     * Maps to: webhookEventSupplier-out-0 in application.yml
     * Binder subscribes to this Flux on startup and sends each item to Kafka.
     * No Kafka imports. No serializer config. No topic wiring in code.
     */
    @Bean
    public Supplier<Flux<WebhookEventDto>> webhookEventSupplier(Sinks.Many<WebhookEventDto> webhookEventSink) {
        return webhookEventSink::asFlux;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
