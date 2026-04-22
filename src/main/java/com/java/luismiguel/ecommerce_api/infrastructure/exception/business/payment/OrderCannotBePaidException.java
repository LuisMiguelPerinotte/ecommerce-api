package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import org.springframework.http.HttpStatus;

public class OrderCannotBePaidException extends PaymentException {
    public OrderCannotBePaidException() {
        super("The Order Cannot Be Paid!", HttpStatus.CONFLICT);
    }
}
