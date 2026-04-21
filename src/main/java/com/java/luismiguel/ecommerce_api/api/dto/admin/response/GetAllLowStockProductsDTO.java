package com.java.luismiguel.ecommerce_api.api.dto.admin.response;

import java.util.UUID;

public record GetAllLowStockProductsDTO(
        UUID productId,
        String name,
        Integer stockQuantity
) {
}
