package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class ProductAlreadyDeactivatedException extends BusinessException {
    public ProductAlreadyDeactivatedException() {
        super("Product Already Deactivated!", HttpStatus.CONFLICT);
    }
}
