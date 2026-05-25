package com.java.luismiguel.ecommerce_api.api.dto.cart.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemQuantityRequestDTO(
        @NotNull(message = "The Quantity is required!")
        @Min(value = 0, message = "The Quantity cannot be negative!")
        Integer quantity
) {
}
