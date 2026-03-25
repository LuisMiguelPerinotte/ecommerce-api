package com.java.luismiguel.ecommerce_api.api.dto.product.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GetProductResponseDTO(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
