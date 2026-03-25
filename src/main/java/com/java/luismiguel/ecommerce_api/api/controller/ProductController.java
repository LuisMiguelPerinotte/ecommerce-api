package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.product.response.GetAllProductsResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.response.GetProductResponseDTO;
import com.java.luismiguel.ecommerce_api.application.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Produto", description = "Operações Get Produtos")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Obter Produtos", description = "Retorna produtos por parâmetros de busca.")
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
    @Operation(summary = "Obter Produto por ID", description = "Retorna um produto pelo o Id")
    public ResponseEntity<GetProductResponseDTO> getProduct(@PathVariable UUID id) {
        return new ResponseEntity<>(productService.getProductById(id), HttpStatus.OK);
    }
}
