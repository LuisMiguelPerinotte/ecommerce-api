package com.java.luismiguel.ecommerce_api.api.dto.order.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOrderRequestDTO(
        UUID addressId,

        @Size(max = 200)
        String userNotes // Opcional
) {
}
