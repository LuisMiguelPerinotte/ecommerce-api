package com.java.luismiguel.ecommerce_api.api.dto.address.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequestDTO(
        @NotBlank(message = "The Street is required!")
        @Size(min = 3, max = 100, message = "The Street cannot be shorter than 3 characters or longer than 100!")
        String street,

        @NotBlank(message = "The Neighborhood is required!")
        @Size(min = 3, max = 100, message = "The Neighborhood cannot be shorter than 3 characters or longer than 100!")
        String neighborhood,

        @NotBlank(message = "The Number is required!")
        @Size(min = 1, max = 10, message = "The Number cannot be shorter than 1 character or longer than 10!")
        String number,

        @Size(max = 100, message = "The Complement cannot be longer than 100 characters!")
        String complement,

        @NotBlank(message = "The Zip Code is required!")
        @Size(min = 8, max = 10, message = "The Zip Code cannot be shorter than 8 characters or longer than 10!")
        String zipCode
) {
}
