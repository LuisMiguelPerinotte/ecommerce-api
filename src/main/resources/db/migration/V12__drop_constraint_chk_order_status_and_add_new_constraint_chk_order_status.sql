ALTER TABLE orders
    DROP CONSTRAINT chk_order_status;

ALTER TABLE orders
    ADD CONSTRAINT chk_order_status
    CHECK (
        order_status IN (
            'PENDING',
            'AWAITING_PAYMENT',
            'PAID',
            'PROCESSING',
            'SHIPPED',
            'DELIVERED',
            'CANCELLED',
            'REFUNDED',
            'PAYMENT_FAILED'
        )
    );