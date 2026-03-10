package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank(message = "The Refresh Token is required!")
        String refreshToken
) {
}
