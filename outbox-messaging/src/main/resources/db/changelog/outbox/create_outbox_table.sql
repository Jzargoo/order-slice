CREATE TABLE outbox (
                        id BIGSERIAL PRIMARY KEY,
                        aggregate_type VARCHAR(255) NOT NULL,
                        event_type VARCHAR(255) NOT NULL,
                        payload TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        processed BOOLEAN DEFAULT FALSE
);

