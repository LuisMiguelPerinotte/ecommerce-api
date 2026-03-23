package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart;

import org.springframework.http.HttpStatus;

public class CartNotFoundException extends CartException {
    public CartNotFoundException() {
        super("Cart Not Found!", HttpStatus.NOT_FOUND);
    }
}
