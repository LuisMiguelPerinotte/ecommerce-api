package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import org.springframework.http.HttpStatus;

public class ErrorCreatingPreferenceException extends PaymentException {
    public ErrorCreatingPreferenceException() {
        super("Error Creating Preference!", HttpStatus.BAD_GATEWAY);
    }
}
