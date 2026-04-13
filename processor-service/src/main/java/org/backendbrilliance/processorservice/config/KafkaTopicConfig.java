package org.backendbrilliance.processorservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.backendbrilliance.common.constants.KafkaConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic webhookEventTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_WEBHOOK_EVENTS)
                .partitions(KafkaConstants.WEBHOOK_EVENTS_PARTITIONS)
                .replicas(KafkaConstants.WEBHOOK_EVENTS_REPLICATION)
                .build();
    }

    @Bean
    public NewTopic webhookEventDLTTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_WEBHOOK_EVENTS_DLT)
                .partitions(1)
                .replicas(KafkaConstants.WEBHOOK_EVENTS_REPLICATION)
                .build();
    }
}
