package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.cart.request.AddCartItemRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.request.UpdateCartItemQuantityRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.response.AddedCartItemDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.response.GetCartResponseDTO;
import com.java.luismiguel.ecommerce_api.application.cart.CartService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart", description = "Operations to manage the authenticated user's shopping cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "public-api")
    @Operation(summary = "Get Cart", description = "Return the authenticated user's cart with items and totals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetCartResponseDTO.class)))
    })
    public ResponseEntity<GetCartResponseDTO> getCart(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(cartService.getCart(user.getUserId()), HttpStatus.OK);
    }


    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Add Item to Cart", description = "Add a product to the authenticated user's cart. If the item exists, quantity is increased.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added to cart", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddedCartItemDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid cart item data", content = @Content)
    })
    public ResponseEntity<AddedCartItemDTO> addCartItem(
            @Valid
            @RequestBody AddCartItemRequestDTO addCartItemRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(cartService.addCartItem(addCartItemRequestDTO, user.getUserId()), HttpStatus.CREATED);
    }


    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Update Cart Item Quantity", description = "Update the quantity of a cart item identified by itemId.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Quantity updated (no content)"),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid quantity", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
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
    @RateLimiter(name = "critical")
    @Operation(summary = "Remove Cart Item", description = "Remove a specific item from the authenticated user's cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removed (no content)"),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
    public ResponseEntity<Void> removeCartItem(@PathVariable UUID itemId) {
        cartService.removeCartItem(itemId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "critical")
    @Operation(summary = "Clear Cart", description = "Remove all items from the authenticated user's cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart cleared (no content)")
    })
    public ResponseEntity<Void> cleanUserCart(@AuthenticationPrincipal User user) {
        cartService.cleanCart(user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
