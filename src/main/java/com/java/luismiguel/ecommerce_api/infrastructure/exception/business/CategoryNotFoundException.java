package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super("Category Not Found!", HttpStatus.NOT_FOUND);
    }
}
