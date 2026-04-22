package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import org.springframework.http.HttpStatus;

public class ErrorCreatingCheckoutException extends PaymentException {
    public ErrorCreatingCheckoutException() {
        super("Error Creating Checkout", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
