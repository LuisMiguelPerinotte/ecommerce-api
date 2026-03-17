package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product;

import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends ProductException {
    public CategoryNotFoundException() {
        super("Category Not Found!", HttpStatus.NOT_FOUND);
    }
}
