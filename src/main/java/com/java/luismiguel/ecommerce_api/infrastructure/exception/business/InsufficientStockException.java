package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException() {
        super("Insufficient Stock!", HttpStatus.valueOf(422));
    }
}
