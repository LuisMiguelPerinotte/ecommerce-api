package com.java.luismiguel.ecommerce_api.api.dto.admin.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustProductStockRequestDTO(
        @NotNull
        @Min(0)
        Integer stock
) {
}
