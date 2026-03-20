package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOrderRequestDTO(
        @NotNull
        UUID addressId,

        @Size(max = 200)
        String userNotes // Opcional
) {
}
