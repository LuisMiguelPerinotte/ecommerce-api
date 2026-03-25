package com.java.luismiguel.ecommerce_api.api.dto.product.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreatedProductResponseDTO(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        LocalDateTime createdAt
) {
}
