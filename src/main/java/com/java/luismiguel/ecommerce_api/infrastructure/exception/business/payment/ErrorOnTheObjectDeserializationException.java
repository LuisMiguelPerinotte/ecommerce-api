package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import org.springframework.http.HttpStatus;

public class ErrorOnTheObjectDeserializationException extends PaymentException {
    public ErrorOnTheObjectDeserializationException() {
        super("Error On The Object Deserialization!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
