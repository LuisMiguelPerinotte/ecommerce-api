package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address;

import org.springframework.http.HttpStatus;

public class InvalidZipCodeException extends AddressException {
    public InvalidZipCodeException() {
        super("Invalid ZipCode!", HttpStatus.valueOf(422));
    }
}
