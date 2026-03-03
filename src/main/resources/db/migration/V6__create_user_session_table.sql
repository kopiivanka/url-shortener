CREATE TABLE IF NOT EXISTS user_session (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                            ip_address VARCHAR(45),
                                            user_agent TEXT,
                                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                            expires_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_user_session_user_id ON user_session(user_id);
CREATE INDEX IF NOT EXISTS idx_user_session_created_at ON user_session(created_at);
