package com.java.luismiguel.ecommerce_api.infrastructure.exception.auth;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException() {
        super("User Not Found!", HttpStatus.NOT_FOUND);
    }
}
