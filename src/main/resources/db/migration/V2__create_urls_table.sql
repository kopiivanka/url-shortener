CREATE TABLE IF NOT EXISTS urls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    short_code VARCHAR(64) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    owner_id UUID NULL REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_urls_owner_id ON urls(owner_id);
CREATE INDEX IF NOT EXISTS idx_urls_created_at ON urls(created_at);