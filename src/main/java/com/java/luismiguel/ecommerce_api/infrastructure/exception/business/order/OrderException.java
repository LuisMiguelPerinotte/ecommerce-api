package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderException extends BusinessException {
    public OrderException(String message, HttpStatus status) {
        super(message, status);
    }
}
