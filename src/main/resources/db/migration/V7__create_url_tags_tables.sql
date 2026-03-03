CREATE TABLE IF NOT EXISTS url_tag (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS url_tag_map (
                                           url_id UUID NOT NULL REFERENCES urls(id) ON DELETE CASCADE,
                                           tag_id UUID NOT NULL REFERENCES url_tag(id) ON DELETE CASCADE,
                                           PRIMARY KEY (url_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_url_tag_map_tag_id ON url_tag_map(tag_id);
