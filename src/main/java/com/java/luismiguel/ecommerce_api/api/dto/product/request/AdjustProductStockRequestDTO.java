package com.java.luismiguel.ecommerce_api.api.dto.product.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustProductStockRequestDTO(
        @NotNull(message = "The quantity is required!")
        @Min(value = 0, message = "The quantity cannot be negative!")
        Integer quantity
) {
}
