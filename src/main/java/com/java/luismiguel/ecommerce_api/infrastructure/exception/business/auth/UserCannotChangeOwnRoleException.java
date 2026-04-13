package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserCannotChangeOwnRoleException extends AuthException {
    public UserCannotChangeOwnRoleException() {
        super("User Cannot Change Their Own Role!", HttpStatus.FORBIDDEN);
    }
}
