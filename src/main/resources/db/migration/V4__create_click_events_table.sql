CREATE TABLE IF NOT EXISTS click_event(
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64) NOT NULL,
    ip              TEXT        NULL,
    user_agent      TEXT        NULL,
    accept_language TEXT        NULL,
    method          TEXT        NULL,
    path            TEXT        NULL,
    headers         JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);