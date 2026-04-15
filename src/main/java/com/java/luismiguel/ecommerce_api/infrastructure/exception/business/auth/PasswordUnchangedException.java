package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class PasswordUnchangedException extends AuthException {
    public PasswordUnchangedException() {
        super("New Password must be different from current!", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
