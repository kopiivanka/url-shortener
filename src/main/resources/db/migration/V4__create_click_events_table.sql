CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS click_events
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    code            VARCHAR(64) NOT NULL,
    ip              TEXT,
    user_agent      TEXT,
    accept_language TEXT,
    method          VARCHAR(16) NOT NULL,
    path            TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);