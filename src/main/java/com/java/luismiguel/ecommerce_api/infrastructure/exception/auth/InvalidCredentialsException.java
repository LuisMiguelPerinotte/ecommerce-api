package com.java.luismiguel.ecommerce_api.infrastructure.exception.auth;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Invalid E-mail or password!", HttpStatus.UNAUTHORIZED);
    }
}
