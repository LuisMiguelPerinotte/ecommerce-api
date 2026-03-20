package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address;

import org.springframework.http.HttpStatus;

public class AddressNotFoundException extends AddressException {
    public AddressNotFoundException() {
        super("Address Not Found!", HttpStatus.NOT_FOUND);
    }
}
