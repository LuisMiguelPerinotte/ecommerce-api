package com.java.luismiguel.ecommerce_api.api.dto.order.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ListOrderItemResponseDTO(
        UUID orderItemId,
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
