package com.java.luismiguel.ecommerce_api.api.dto.admin.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustProductStockRequestDTO(
        @NotNull(message = "The Stock is required!")
        @Min(value = 0, message = "The Stock cannot be negative!")
        Integer stock
) {
}
