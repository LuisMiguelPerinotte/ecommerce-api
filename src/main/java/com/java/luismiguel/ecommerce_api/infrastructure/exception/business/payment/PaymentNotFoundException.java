package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException() {
        super("Payment Not Found!", HttpStatus.NOT_FOUND);
    }
}
