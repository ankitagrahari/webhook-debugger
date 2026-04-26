-- V3__users_index.sql
-- Index for email lookup on login (called on every authentication)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);