package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth;

import org.springframework.http.HttpStatus;

public class UserAccountIsAlreadyActivatedException extends AuthException {
  public UserAccountIsAlreadyActivatedException() {
    super("User Account Is Already Activated!", HttpStatus.CONFLICT);
  }
}
