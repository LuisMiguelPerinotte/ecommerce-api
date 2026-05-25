package com.java.luismiguel.ecommerce_api.api.dto.product.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequestDTO(
        @NotBlank(message = "The Name is required!")
        @Size(min = 6, max = 200, message = "The Name cannot be shorter than 6 characters or longer than 200!")
        String name,

        @Size(min = 10, max = 2000, message = "The Description cannot be shorter than 10 characters or longer than 2000!")
        String description,

        @NotNull(message = "The Price is required!")
        @DecimalMin(value = "0.01", message = "The Price must be at least 0.01!")
        BigDecimal price,

        @NotNull(message = "The Stock Quantity is required!")
        @Min(value = 0, message = "The Stock Quantity cannot be negative!")
        Integer stockQuantity,

        @NotNull(message = "The Category Id is required!")
        UUID categoryId
) {
}
