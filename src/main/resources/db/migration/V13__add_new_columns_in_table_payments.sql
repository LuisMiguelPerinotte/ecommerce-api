ALTER TABLE payments
    ADD COLUMN payment_intent_id VARCHAR UNIQUE,
    ADD COLUMN failure_reason VARCHAR,
    ADD COLUMN failed_at TIMESTAMP;
