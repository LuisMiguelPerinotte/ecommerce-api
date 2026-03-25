package com.java.luismiguel.ecommerce_api.api.dto.category.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetAllActiveCategoriesDTO(
        UUID categoryId,
        String name,
        String slug,
        LocalDateTime createdAt

) {
}
