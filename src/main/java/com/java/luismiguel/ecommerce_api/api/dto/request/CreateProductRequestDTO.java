package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequestDTO(
        @NotBlank
        @Size(min = 6, max = 200)
        String name,

        @Size(min = 10, max = 2000)
        String description,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal price,

        @NotNull
        @Min(0)
        Integer stockQuantity,

        @NotNull
        UUID categoryId
) {
}
