package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class ProductException extends BusinessException {
    public ProductException(String message, HttpStatus status) {
        super(message, status);
    }
}
