package com.java.luismiguel.ecommerce_api.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AddedCartItemDTO(
        UUID cartItemId,
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
