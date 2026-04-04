package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address;

import org.springframework.http.HttpStatus;

public class AddressAlreadyExistsException extends AddressException {
    public AddressAlreadyExistsException() {
        super("Address Already Exists!", HttpStatus.CONFLICT);
    }
}
