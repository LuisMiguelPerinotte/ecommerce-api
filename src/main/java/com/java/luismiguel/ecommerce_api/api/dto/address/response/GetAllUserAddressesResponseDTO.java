package com.java.luismiguel.ecommerce_api.api.dto.address.response;

import java.util.UUID;

public record GetAllUserAddressesResponseDTO(
        UUID addressId,
        String street,
        String number,
        String city,
        String state,
        Boolean isDefault
) {
}
