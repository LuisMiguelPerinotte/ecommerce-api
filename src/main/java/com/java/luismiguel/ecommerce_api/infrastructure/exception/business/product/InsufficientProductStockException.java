package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product;

import org.springframework.http.HttpStatus;

public class InsufficientProductStockException extends ProductException {
    public InsufficientProductStockException() {
        super("Insufficient Stock!", HttpStatus.valueOf(422));
    }
}
