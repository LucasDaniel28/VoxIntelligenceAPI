CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE')),
    before_data TEXT,
    after_data TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX idx_audit_events_occurred_at ON audit_events(occurred_at);
