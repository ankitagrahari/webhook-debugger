package org.backendbrilliance.processorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "webhook_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "endpoint_id", nullable = false)
    private UUID endpointId;

    @Column(nullable = false, length = 10)
    private String method;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> headers;

    @Column(columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_params", columnDefinition = "jsonb")
    private Map<String, String> queryParams;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "body_size")
    private Long bodySize;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
}
