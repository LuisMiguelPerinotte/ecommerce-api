-- Table payments
CREATE TABLE payments(
    payment_id UUID PRIMARY KEY,
    order_id UUID,
    mp_preference_id VARCHAR NOT NULL,
    mp_payment_id VARCHAR,
    external_reference VARCHAR NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_status CHECK (status IN ('CREATED', 'APPROVED', 'REJECTED', 'PENDING', 'REFUNDED')),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE SET NULL
)