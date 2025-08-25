CREATE TABLE saga_state (
    id varchar(128) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    error_message TEXT,
    current_target_saga_step VARCHAR(64) NOT NULL,
    step varchar(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);