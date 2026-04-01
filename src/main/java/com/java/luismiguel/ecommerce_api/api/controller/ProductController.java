package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.product.response.GetAllProductsResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.response.GetProductResponseDTO;
import com.java.luismiguel.ecommerce_api.application.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product listing and retrieval endpoints")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get Products", description = "Retrieve paginated products with optional filters: name, categoryId, minPrice, maxPrice.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of products", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetAllProductsResponseDTO.class)))
    })
    public ResponseEntity<Page<GetAllProductsResponseDTO>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return new ResponseEntity<>(productService.getAllProducts(name, categoryId, minPrice, maxPrice, pageable), HttpStatus.OK);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get Product by ID", description = "Return detailed product information by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<GetProductResponseDTO> getProduct(@PathVariable UUID id) {
        return new ResponseEntity<>(productService.getProductById(id), HttpStatus.OK);
    }
}
