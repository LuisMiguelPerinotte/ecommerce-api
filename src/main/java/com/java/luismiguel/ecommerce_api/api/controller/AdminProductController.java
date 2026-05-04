package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.product.request.AdjustProductStockRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.request.CreateProductRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.request.UpdateProductRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.response.CreatedProductResponseDTO;
import com.java.luismiguel.ecommerce_api.application.product.ProductService;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
@Tag(name = "Admin Products", description = "APIs for administrators to manage products: create, update, stock adjustments, activate/deactivate and delete.")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Create Product", description = "Create a new product. Requires ADMIN privileges.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreatedProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid product data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Product with same SKU or identifier already exists (conflict)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content)
    })
    public ResponseEntity<CreatedProductResponseDTO> createProduct(
            @Valid
            @RequestBody CreateProductRequestDTO createProductRequestDTO
    ) {
        return new ResponseEntity<>(productService.createProduct(createProductRequestDTO), HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Update Product", description = "Update details of an existing product by ID. Requires ADMIN privileges.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product updated (no content)"),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid product fields", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Void> updateProduct(
            @Valid
            @PathVariable UUID id,
            @RequestBody UpdateProductRequestDTO updateProductRequestDTO
    ) {
        productService.updateProduct(id, updateProductRequestDTO);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Adjust Product Stock", description = "Adjust the stock quantity of a product. Requires ADMIN privileges.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Stock adjusted (no content)"),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid stock quantity", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Void> adjustProductStock(
            @Valid
            @PathVariable UUID id,
            @RequestBody AdjustProductStockRequestDTO adjustProductStockDTO
    ) {
        productService.adjustStock(id, adjustProductStockDTO);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Activate Product", description = "Activate a previously deactivated product. Requires ADMIN privileges.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product activated (no content)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Void> activateProduct(@PathVariable UUID id) {
        productService.activateProduct(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Deactivate Product", description = "Deactivate a product, making it unavailable. Requires ADMIN privileges.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deactivated (no content)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Void> deactivateProduct(@PathVariable UUID id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
