package com.java.luismiguel.ecommerce_api.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatedCategoryResponseDTO(
        UUID categoryId,
        String name,
        String description,
        String slug,
        Boolean active,
        LocalDateTime createdAt
) {
}
