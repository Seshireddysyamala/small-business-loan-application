CREATE TABLE underwriting_decisions (
    decision_id VARCHAR(36) PRIMARY KEY,
    source_event_id VARCHAR(36) NOT NULL,
    application_id BIGINT NOT NULL,
    decision VARCHAR(30) NOT NULL,
    approved_amount DECIMAL(19, 2),
    reason_codes VARCHAR(500) NOT NULL,
    policy_version VARCHAR(50) NOT NULL,
    decided_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_underwriting_source_event UNIQUE (source_event_id)
);

CREATE INDEX idx_underwriting_application_id
    ON underwriting_decisions (application_id);
