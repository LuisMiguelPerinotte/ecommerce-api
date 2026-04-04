package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address;

import org.springframework.http.HttpStatus;

public class AddressIsAlreadyDefaultException extends AddressException {
    public AddressIsAlreadyDefaultException() {
        super("Address Is Already Default!", HttpStatus.CONFLICT);
    }
}
