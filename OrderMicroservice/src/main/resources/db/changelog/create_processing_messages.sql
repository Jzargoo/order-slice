CREATE TABLE processing_messages (
    id varchar(128) PRIMARY KEY,
    message_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)