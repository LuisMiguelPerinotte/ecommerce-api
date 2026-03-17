package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart;

import org.springframework.http.HttpStatus;

public class CartIsEmptyException extends CartException {
    public CartIsEmptyException() {
        super("The Cart Is Already Empty!", HttpStatus.valueOf(422));
    }
}
