package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequestDTO(
        @NotBlank(message = "The Name is required!")
        @Size(max = 100, message = "The maximum number of characters is 100.")
        String name,

        @Size(max = 500, message = "The maximum number of characters is 500.")
        String description
) {
}
