package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank (message = "The Email is required!")
        String email,

        @NotBlank (message = "The Password is required!")
        String password
) {
}
