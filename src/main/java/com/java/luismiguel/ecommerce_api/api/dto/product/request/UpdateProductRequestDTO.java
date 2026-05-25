package com.java.luismiguel.ecommerce_api.api.dto.product.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequestDTO(
        @Size(min = 6, max = 200, message = "The Name cannot be shorter than 6 characters or longer than 200!")
        String name,

        @Size(min = 10, max = 2000, message = "The Description cannot be shorter than 10 characters or longer than 2000!")
        String description,

        @DecimalMin(value = "0.01", message = "The Price must be at least 0.01!")
        BigDecimal price,

        UUID categoryId

) {
}
