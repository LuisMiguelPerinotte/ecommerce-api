package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequestDTO(
        @Size(min = 6, max = 200)
        String name,

        @Size(min = 10, max = 2000)
        String description,

        @DecimalMin("0.01")
        BigDecimal price,

        UUID categoryId

) {
}
