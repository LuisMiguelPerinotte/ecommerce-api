package com.java.luismiguel.ecommerce_api.api.dto.auth.response;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
