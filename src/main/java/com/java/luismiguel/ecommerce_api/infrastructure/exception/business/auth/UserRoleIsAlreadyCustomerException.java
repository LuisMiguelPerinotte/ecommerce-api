package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserRoleIsAlreadyCustomerException extends AuthException {
    public UserRoleIsAlreadyCustomerException() {
        super("User Role Is Already Customer", HttpStatus.CONFLICT);
    }
}
