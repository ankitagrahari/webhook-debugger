CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email         VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       tier          VARCHAR(20) NOT NULL DEFAULT 'FREE',
                       created_at    TIMESTAMP NOT NULL DEFAULT now(),
                       updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE endpoints (
                           id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           slug       VARCHAR(32) UNIQUE NOT NULL,
                           user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
                           label      VARCHAR(100),
                           created_at TIMESTAMP NOT NULL DEFAULT now(),
                           expires_at TIMESTAMP
);

CREATE INDEX idx_endpoints_slug    ON endpoints(slug);
CREATE INDEX idx_endpoints_user_id ON endpoints(user_id);

CREATE TABLE webhook_requests (
                                  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  endpoint_id  UUID NOT NULL REFERENCES endpoints(id) ON DELETE CASCADE,
                                  method       VARCHAR(10) NOT NULL,
                                  headers      JSONB,
                                  body         TEXT,
                                  query_params JSONB,
                                  source_ip    VARCHAR(45),
                                  content_type VARCHAR(255),
                                  body_size    BIGINT DEFAULT 0,
                                  received_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_requests_endpoint_id  ON webhook_requests(endpoint_id);
CREATE INDEX idx_requests_received_at  ON webhook_requests(received_at DESC);
CREATE INDEX idx_requests_method       ON webhook_requests(method);
CREATE INDEX idx_requests_headers      ON webhook_requests USING gin(headers);