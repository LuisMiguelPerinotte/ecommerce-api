package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserRoleIsAlreadyAdminException extends AuthException {
    public UserRoleIsAlreadyAdminException() {
        super("User Role Is Already Admin", HttpStatus.CONFLICT);
    }
}
