package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import org.springframework.http.HttpStatus;

public class PaymentIsAlreadyInProgressException extends PaymentException {
    public PaymentIsAlreadyInProgressException() {
        super("A Payment Is Already In Progress!", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
