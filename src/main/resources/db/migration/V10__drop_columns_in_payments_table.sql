ALTER TABLE payments
    DROP COLUMN updated_at,
    DROP COLUMN external_reference,
    DROP COLUMN mp_preference_id,
    DROP COLUMN mp_payment_id;
