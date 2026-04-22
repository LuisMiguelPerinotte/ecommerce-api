package com.java.luismiguel.ecommerce_api.api.dto.payment.response;

public record CheckoutResponseDTO(
        String stripePaymentUrl
) {
}
