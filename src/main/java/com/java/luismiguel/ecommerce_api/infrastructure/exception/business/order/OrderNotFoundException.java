package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException() {
        super("Order Not Found!", HttpStatus.NOT_FOUND);
    }
}
