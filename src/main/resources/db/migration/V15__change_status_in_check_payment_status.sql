ALTER TABLE payments
DROP CONSTRAINT chk_payment_status;

ALTER TABLE payments
    ADD CONSTRAINT chk_payment_status
        CHECK (
            status IN (
                       'CREATED',
                       'APPROVED',
                       'FAILED',
                       'EXPIRED',
                       'REFUNDED'
                )
            );