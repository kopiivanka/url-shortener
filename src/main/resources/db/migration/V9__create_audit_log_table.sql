CREATE TABLE IF NOT EXISTS audit_log (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         user_id UUID REFERENCES users(id) ON DELETE SET NULL,
                                         action VARCHAR(64) NOT NULL,
                                         entity VARCHAR(64),
                                         entity_id UUID,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity_entity_id ON audit_log(entity, entity_id);
