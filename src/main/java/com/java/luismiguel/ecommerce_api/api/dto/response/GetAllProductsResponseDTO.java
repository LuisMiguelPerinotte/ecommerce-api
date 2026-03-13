package com.java.luismiguel.ecommerce_api.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record GetAllProductsResponseDTO(
        UUID productId,
        String name,
        BigDecimal price,
        String categoryName
) {
}
