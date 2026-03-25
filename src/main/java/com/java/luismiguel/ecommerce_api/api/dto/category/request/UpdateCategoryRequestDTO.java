package com.java.luismiguel.ecommerce_api.api.dto.category.request;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequestDTO(
        @Size(min = 4, max = 100, message = "The maximum number of characters is 100.")
        String name,

        @Size(min = 6, max = 500, message = "The maximum number of characters is 500.")
        String description
) {
}
