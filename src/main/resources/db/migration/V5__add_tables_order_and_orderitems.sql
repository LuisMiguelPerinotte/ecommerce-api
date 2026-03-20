-- Table orders
CREATE TABLE orders(
    order_id UUID PRIMARY KEY,
    user_id UUID,
    shipping_address_id UUID,
    order_status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(10, 2),
    mp_preference_id VARCHAR(255),
    user_notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_status CHECK (order_status IN ('PENDING', 'AWAITING_PAYMENT', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED' , 'REFUNDED')),

    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_order_address FOREIGN KEY (shipping_address_id) REFERENCES addresses(address_id) ON DELETE SET NULL

);

-- Table orders
CREATE TABLE order_items(
    order_item_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID,
    product_name VARCHAR(200),
    product_sku VARCHAR,
    unit_price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,

    CONSTRAINT fk_orders_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_items_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL
);