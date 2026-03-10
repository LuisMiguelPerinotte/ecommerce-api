package com.java.luismiguel.ecommerce_api.infrastructure.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
