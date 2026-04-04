package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address;

import org.springframework.http.HttpStatus;

public class NoDefaultAddressRegisteredException extends AddressException {
    public NoDefaultAddressRegisteredException() {
        super("No Default Address Registered", HttpStatus.valueOf(422));
    }
}
