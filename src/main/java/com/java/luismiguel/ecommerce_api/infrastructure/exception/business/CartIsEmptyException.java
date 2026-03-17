package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class CartIsEmptyException extends BusinessException {
    public CartIsEmptyException() {
        super("The Cart Is Already Empty!", HttpStatus.valueOf(422));
    }
}
