package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserEmailAlreadyRegisteredException extends AuthException {
    public UserEmailAlreadyRegisteredException() {
        super("E-mail is already registered!", HttpStatus.CONFLICT);
    }
}
