package com.java.luismiguel.ecommerce_api.infrastructure.exception.business;

import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends BusinessException {
    public CategoryAlreadyExistsException() {
        super("Category Already Exists!", HttpStatus.CONFLICT);
    }
}
