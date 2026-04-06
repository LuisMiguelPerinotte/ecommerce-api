package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.address.request.CreateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.request.UpdateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.CreatedAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAllUserAddressesResponseDTO;
import com.java.luismiguel.ecommerce_api.application.address.AddressService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/addresses")
@Tag(name = "Addresses", description = "Operations to manage user shipping addresses")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Create Address", description = "Create a new shipping address for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreatedAddressResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid address data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate address for user (address already exists)", content = @Content)
    })
    public ResponseEntity<CreatedAddressResponseDTO> createAddress(
            @Valid
            @RequestBody CreateAddressRequestDTO createAddressRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(addressService.createAddress(createAddressRequestDTO, user), HttpStatus.CREATED);
    }


    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "public-api")
    @Operation(summary = "Get User Addresses", description = "Return a pageable list of addresses for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of user addresses returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetAllUserAddressesResponseDTO.class)))
    })
    public ResponseEntity<Page<GetAllUserAddressesResponseDTO>> getUserAddresses(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return new ResponseEntity<>(addressService.getUserAddresses(user, pageable), HttpStatus.OK);
    }


    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "public-api")
    @Operation(summary = "Get Address by ID", description = "Returns an address by its ID for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetAddressResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Address not found for the authenticated user", content = @Content)
    })
    public ResponseEntity<GetAddressResponseDTO> getAddressById(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(addressService.getAddressById(addressId, user.getUserId()), HttpStatus.OK);
    }


    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "critical")
    @Operation(summary = "Delete Address by ID", description = "Soft-delete an address by its ID. Only the owner can delete their address.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted (no content)"),
            @ApiResponse(responseCode = "404", description = "Address not found or not owned by user", content = @Content)
    })
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        addressService.deleteAddressById(addressId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Partially Update Address", description = "Partially update address fields for the authenticated user's address.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address updated (no content)"),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid address fields", content = @Content),
            @ApiResponse(responseCode = "404", description = "Address not found or not owned by user", content = @Content)
    })
    public ResponseEntity<Void> updateAddress(
            @Valid
            @PathVariable UUID addressId,
            @RequestBody UpdateAddressRequestDTO addressRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        addressService.updateAddress(addressId, addressRequestDTO, user);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{addressId}/default")
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Change Default Address", description = "Set the specified address as the default for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Default address changed (no content)"),
            @ApiResponse(responseCode = "404", description = "Address not found or not owned by user", content = @Content)
    })
    public ResponseEntity<Void> changeDefaultAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal User user
    ) {
        addressService.setDefaultAddress(addressId, user);
        return ResponseEntity.noContent().build();
    }
}
