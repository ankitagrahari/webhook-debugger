-- V2__tier_enforcement.sql

-- Safely add CHECK constraint using DO block (PostgreSQL compatible)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_users_tier'
    ) THEN
ALTER TABLE users
    ADD CONSTRAINT chk_users_tier
        CHECK (tier IN ('FREE', 'PRO', 'TEAM'));

END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_endpoint_active'
    ) THEN
ALTER TABLE endpoints
    ADD CONSTRAINT chk_endpoint_active
        CHECK (expires_at IS NULL OR expires_at > NOW());

END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_users_tier ON users(tier);