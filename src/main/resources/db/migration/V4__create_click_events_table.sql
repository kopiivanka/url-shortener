CREATE TABLE click_event
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code_uuid  UUID  NOT NULL,
    metadata   JSONB NOT NULL,
    created_at TIMESTAMPTZ      DEFAULT now()
);