package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product;

import org.springframework.http.HttpStatus;

public class ProductAlreadyActivatedException extends ProductException {
    public ProductAlreadyActivatedException() {
        super("Product Already Activated!", HttpStatus.CONFLICT);
    }
}
