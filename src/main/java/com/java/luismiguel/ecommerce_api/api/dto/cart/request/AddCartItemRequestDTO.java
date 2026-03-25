package com.java.luismiguel.ecommerce_api.api.dto.cart.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCartItemRequestDTO(
        @NotNull
        UUID productId,

        @NotNull
        @Min(0)
        Integer quantity
) {
}
