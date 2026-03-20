package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class AddressException extends BusinessException {
    public AddressException(String message, HttpStatus status) {
        super(message, status);
    }
}
