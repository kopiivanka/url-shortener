CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS click_events
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    code        VARCHAR(64) NOT NULL,
    status      VARCHAR(16) NOT NULL,
    url_id      UUID        NOT NULL REFERENCES urls (id) ON DELETE CASCADE,
    referer     TEXT,
    user_agent  TEXT
);
