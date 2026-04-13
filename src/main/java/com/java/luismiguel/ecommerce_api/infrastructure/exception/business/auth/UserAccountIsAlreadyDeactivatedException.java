package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserAccountIsAlreadyDeactivatedException extends AuthException {
    public UserAccountIsAlreadyDeactivatedException() {
        super("User Account Is Already Deactivated!", HttpStatus.CONFLICT);
    }
}
