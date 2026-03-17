package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends AuthException {
    public InvalidPasswordException() {
        super("Current password is incorrect!", HttpStatus.UNAUTHORIZED);
    }
}
