package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentException extends BusinessException {
    public PaymentException(String message, HttpStatus status) {
        super(message, status);
    }
}
