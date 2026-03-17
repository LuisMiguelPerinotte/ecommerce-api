package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.request.AdjustProductStockDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.CreateProductRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.UpdateProductRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.CreatedProductResponseDTO;
import com.java.luismiguel.ecommerce_api.application.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
@Tag(name = "Gerenciamento de Produtos do Administrador", description = "APIs para administradores gerenciarem produtos, incluindo criação, atualizações, ajustes de estoque, ativação, desativação e exclusão.")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar Produto (POST)", description = "Cria um novo produto no sistema. Requer privilégios de administrador.")
    public ResponseEntity<CreatedProductResponseDTO> createProduct(
            @Valid
            @RequestBody CreateProductRequestDTO createProductRequestDTO
    ) {
        return new ResponseEntity<>(productService.createProduct(createProductRequestDTO), HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar Produto (PATCH)", description = "Atualiza os detalhes de um produto existente identificado pelo seu ID. Requer privilégios de administrador.")
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
    @Operation(summary = "Ajustar Estoque do Produto (PATCH)", description = "Ajusta a quantidade de estoque de um produto. Requer privilégios de administrador.")
    public ResponseEntity<Void> adjustProductStock(
            @Valid
            @PathVariable UUID id,
            @RequestBody AdjustProductStockDTO adjustProductStockDTO
    ) {
        productService.adjustStock(id, adjustProductStockDTO);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir Produto (DELETE)", description = "Exclui suavemente um produto, marcando-o como inativo. Requer privilégios de administrador.")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar Produto (PATCH)", description = "Ativa um produto previamente desativado. Requer privilégios de administrador.")
    public ResponseEntity<Void> activateProduct(@PathVariable UUID id) {
        productService.activateProduct(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar Produto (PATCH)", description = "Desativa um produto, tornando-o indisponível. Requer privilégios de administrador.")
    public ResponseEntity<Void> deactivateProduct(@PathVariable UUID id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
