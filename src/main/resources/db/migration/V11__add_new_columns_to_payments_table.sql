ALTER TABLE payments
    ADD COLUMN stripe_session_id VARCHAR UNIQUE,
    ADD COLUMN currency VARCHAR(50),
    ADD COLUMN paid_at TIMESTAMP;
