package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class CartItemNotFoundException extends BusinessException {
    public CartItemNotFoundException() {
        super("Cart Item Not Found!", HttpStatus.NOT_FOUND);
    }
}
