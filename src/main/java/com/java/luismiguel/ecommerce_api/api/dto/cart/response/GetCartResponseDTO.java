package com.java.luismiguel.ecommerce_api.api.dto.cart.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GetCartResponseDTO(
        UUID cartId,
        List<ListCartItemsResponseDTO> items,
        Integer totalItems,
        BigDecimal totalAmount
) {
}
