package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class AuthException extends BusinessException {
    public AuthException(String message, HttpStatus status) {
        super(message, status);
    }
}
