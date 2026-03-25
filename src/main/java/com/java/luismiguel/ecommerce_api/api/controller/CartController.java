package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.cart.request.AddCartItemRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.request.UpdateCartItemQuantityRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.response.AddedCartItemDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.response.GetCartResponseDTO;
import com.java.luismiguel.ecommerce_api.application.cart.CartService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@Tag(name = "Carrinho", description = "")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Obter Carrinho", description = "")
    public ResponseEntity<GetCartResponseDTO> getCart(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(cartService.getCart(user.getUserId()), HttpStatus.OK);
    }


    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Adicionar Items ao Carrinho", description = "")
    public ResponseEntity<AddedCartItemDTO> addCartItem(
            @Valid
            @RequestBody AddCartItemRequestDTO addCartItemRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(cartService.addCartItem(addCartItemRequestDTO, user.getUserId()), HttpStatus.CREATED);
    }


    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Atualizar Quantidade de Items", description = "")
    public ResponseEntity<Void> updateCartItemQuantity(
            @Valid
            @PathVariable UUID itemId,
            @RequestBody UpdateCartItemQuantityRequestDTO updateCartItemQuantityRequestDTO
    ) {
        cartService.updateCartItemQuantity(updateCartItemQuantityRequestDTO, itemId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Remover Item do Carrinho", description = "")
    public ResponseEntity<Void> removeCartItem(@PathVariable UUID itemId) {
        cartService.removeCartItem(itemId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Limpar Carrinho", description = "")
    public ResponseEntity<Void> cleanUserCart(@AuthenticationPrincipal User user) {
        cartService.cleanCart(user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
