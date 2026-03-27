package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.address.request.CreateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.request.UpdateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.CreatedAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAllUserAddressesResponseDTO;
import com.java.luismiguel.ecommerce_api.application.address.AddressService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Endereços", description = "")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Criar Endereço", description = "")
    public ResponseEntity<CreatedAddressResponseDTO> createAddress(
            @Valid
            @RequestBody CreateAddressRequestDTO createAddressRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(addressService.createAddress(createAddressRequestDTO, user), HttpStatus.CREATED);
    }


    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Obter Todos Endereços", description = "")
    public ResponseEntity<Page<GetAllUserAddressesResponseDTO>> getUserAddresses(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return new ResponseEntity<>(addressService.getUserAddresses(user, pageable), HttpStatus.OK);
    }


    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Obter Endereço Por Id", description = "")
    public ResponseEntity<GetAddressResponseDTO> getAddressById(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(addressService.getAddressById(addressId, user.getUserId()), HttpStatus.OK);
    }


    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Deletar Endereço Por Id", description = "")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        addressService.deleteAddressById(addressId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Atualizar Endereço Parcialmente", description = "")
    public ResponseEntity<Void> updateAddress(
            @Valid
            @PathVariable UUID addressId,
            @RequestBody UpdateAddressRequestDTO addressRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        addressService.updateAddress(addressId, addressRequestDTO, user);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Alterar Endereço Padrão", description = "")
    public ResponseEntity<Void> changeDefaultAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal User user
    ) {
        addressService.setDefaultAddress(addressId, user);
        return ResponseEntity.noContent().build();
    }
}
