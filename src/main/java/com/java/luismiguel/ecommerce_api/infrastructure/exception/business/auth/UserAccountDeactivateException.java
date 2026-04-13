package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserAccountDeactivateException extends AuthException {
    public UserAccountDeactivateException() {
        super("User Account Has Been Deactivated!", HttpStatus.FORBIDDEN);
    }
}
