CREATE TABLE IF NOT EXISTS url_access_limit (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                url_id UUID NOT NULL UNIQUE REFERENCES urls(id) ON DELETE CASCADE,
                                                max_clicks INTEGER CHECK (max_clicks IS NULL OR max_clicks >= 0),
                                                max_clicks_per_day INTEGER CHECK (max_clicks_per_day IS NULL OR max_clicks_per_day >= 0)
);

CREATE INDEX IF NOT EXISTS idx_url_access_limit_url_id ON url_access_limit(url_id);
