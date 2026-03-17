package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product;

import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends ProductException {
    public CategoryAlreadyExistsException() {
        super("Category Already Exists!", HttpStatus.CONFLICT);
    }
}
