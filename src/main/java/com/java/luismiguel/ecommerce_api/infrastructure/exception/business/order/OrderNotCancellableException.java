package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order;

import org.springframework.http.HttpStatus;

public class OrderNotCancellableException extends OrderException {
    public OrderNotCancellableException() {
        super("Order Cannot Be Cancelled In Its Current Status!", HttpStatus.valueOf(422));
    }
}
