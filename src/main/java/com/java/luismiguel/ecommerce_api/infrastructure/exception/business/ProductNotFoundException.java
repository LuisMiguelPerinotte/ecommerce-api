package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException() {
        super("Product Not Found!", HttpStatus.NOT_FOUND);
    }
}
