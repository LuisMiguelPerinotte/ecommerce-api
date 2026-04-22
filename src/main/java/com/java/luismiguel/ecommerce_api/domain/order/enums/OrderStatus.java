package com.java.luismiguel.ecommerce_api.domain.order.enums;

public enum OrderStatus {
    PENDING,
    AWAITING_PAYMENT,
    PAYMENT_FAILED,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
