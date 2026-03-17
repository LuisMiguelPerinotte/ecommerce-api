package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ProductException {
    public ProductNotFoundException() {
        super("Product Not Found!", HttpStatus.NOT_FOUND);
    }
}
