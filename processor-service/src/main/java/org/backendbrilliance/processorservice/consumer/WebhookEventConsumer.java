package org.backendbrilliance.processorservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.backendbrilliance.common.constants.KafkaConstants;
import org.backendbrilliance.common.dto.WebhookEventDto;
import org.backendbrilliance.processorservice.service.WebhookProcessorService;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventConsumer {

    private final WebhookProcessorService webhookProcessorService;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = ".DLT",
            autoCreateTopics = "true"
    )
    @KafkaListener(
            topics = KafkaConstants.TOPIC_WEBHOOK_EVENTS,
            groupId = KafkaConstants.GROUP_PROCESSOR,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, WebhookEventDto> record,
            Acknowledgment acknowledgement){
        WebhookEventDto event = record.value();
        log.debug("Consuming webhook event [id={}, slug={}, partition={}, offset={}]",
                event.getId(), event.getEndpointSlug(), record.partition(), record.offset());
        try{
            webhookProcessorService.process(event);
            acknowledgement.acknowledge();
        } catch (Exception e) {
            log.error("Consuming webhook event failed [id={}]: {}", event.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaConstants.TOPIC_WEBHOOK_EVENTS_DLT,
            groupId = KafkaConstants.GROUP_PROCESSOR + "-dlt"
    )
    public void consumeDLT(ConsumerRecord<String, WebhookEventDto> record){
        log.error("Webhook event landed in DLT [key={}, partition={}, offset={}]",
                record.key(), record.partition(), record.offset());
    }
}
