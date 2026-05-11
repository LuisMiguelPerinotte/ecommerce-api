package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserRegistrationDataIntegrityException extends AuthException {
    public UserRegistrationDataIntegrityException() {
        super("Registration Data Integrity Violation!", HttpStatus.CONFLICT);
    }
}
