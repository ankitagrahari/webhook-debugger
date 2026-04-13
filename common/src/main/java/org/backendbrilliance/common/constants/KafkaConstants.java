package org.backendbrilliance.common.constants;

public final class KafkaConstants {

    private KafkaConstants() {}

    public static final String TOPIC_WEBHOOK_EVENTS = "webhook-events";
    public static final String TOPIC_WEBHOOK_EVENTS_DLT = "webhook-events.DLT";
    public static final String GROUP_PROCESSOR = "webhook-processor-group";
    public static final int WEBHOOK_EVENTS_PARTITIONS = 3;
    public static final short WEBHOOK_EVENTS_REPLICATION = 1;
}