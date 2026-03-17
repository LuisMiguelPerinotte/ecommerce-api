package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends AuthException {
    public InvalidRefreshTokenException() {
        super("Invalid Refresh Token!", HttpStatus.UNAUTHORIZED);
    }
}
