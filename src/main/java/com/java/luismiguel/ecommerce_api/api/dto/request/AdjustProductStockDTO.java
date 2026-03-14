package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustProductStockDTO(
        @NotNull(message = "The quantity is required!")
        @Min(0)
        Integer quantity
) {
}
