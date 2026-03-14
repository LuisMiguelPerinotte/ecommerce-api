package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class ProductAlreadyActivatedException extends BusinessException {
    public ProductAlreadyActivatedException() {
        super("Product Already Activated!", HttpStatus.CONFLICT);
    }
}
