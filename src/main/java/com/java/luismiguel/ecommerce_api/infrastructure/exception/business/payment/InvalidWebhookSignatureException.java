package com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment;

import org.springframework.http.HttpStatus;

public class InvalidWebhookSignatureException extends PaymentException {
    public InvalidWebhookSignatureException() {
        super("Invalid Webhook Signature!", HttpStatus.UNAUTHORIZED);
    }
}
