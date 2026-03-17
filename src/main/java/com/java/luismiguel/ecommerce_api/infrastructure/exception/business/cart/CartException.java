package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class CartException extends BusinessException {
    public CartException(String message, HttpStatus status) {
        super(message, status);
    }
}
