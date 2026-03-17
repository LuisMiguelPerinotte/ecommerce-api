package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product;

import org.springframework.http.HttpStatus;

public class ProductAlreadyDeactivatedException extends ProductException {
    public ProductAlreadyDeactivatedException() {
        super("Product Already Deactivated!", HttpStatus.CONFLICT);
    }
}
