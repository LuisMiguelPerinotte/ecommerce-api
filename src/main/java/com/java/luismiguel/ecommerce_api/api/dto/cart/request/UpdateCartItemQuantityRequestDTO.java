package com.java.luismiguel.ecommerce_api.api.dto.cart.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemQuantityRequestDTO(
        @NotNull
        @Min(0)
        Integer quantity
) {
}
