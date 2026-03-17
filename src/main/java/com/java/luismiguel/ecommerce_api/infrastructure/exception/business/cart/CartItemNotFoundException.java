package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart;

import org.springframework.http.HttpStatus;

public class CartItemNotFoundException extends CartException {
    public CartItemNotFoundException() {
        super("Cart Item Not Found!", HttpStatus.NOT_FOUND);
    }
}
