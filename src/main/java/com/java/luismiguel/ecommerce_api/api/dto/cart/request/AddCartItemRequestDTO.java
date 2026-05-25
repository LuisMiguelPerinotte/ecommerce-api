package com.java.luismiguel.ecommerce_api.api.dto.cart.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCartItemRequestDTO(
        @NotNull(message = "The Product Id is required!")
        UUID productId,

        @NotNull(message = "The Quantity is required!")
        @Min(value = 0, message = "The Quantity cannot be negative!")
        Integer quantity
) {
}
