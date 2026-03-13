package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.request.CreateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.UpdateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.CreatedCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.application.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/categories")
@Tag(name = "Gerenciamento de Categorias do Administrador", description = "APIs para administradores gerenciarem categorias, incluindo criação, atualização e exclusão.")
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar Categoria", description = "Cria uma categoria com as informações do request.")
    public ResponseEntity<CreatedCategoryResponseDTO> createCategory(
            @Valid
            @RequestBody CreateCategoryRequestDTO createCategoryRequestDTO
    ) {
        return new ResponseEntity<>(categoryService.createCategory(createCategoryRequestDTO), HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar Categoria", description = "edita uma categoria pelo id dado como parâmetro.")
    public ResponseEntity<Void> editCategory(
            @Valid
            @PathVariable UUID id,
            @RequestBody UpdateCategoryRequestDTO updateCategoryRequestDTO
    ) {
        categoryService.updateCategory(id, updateCategoryRequestDTO);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir Categoria", description = "faz um soft delete na categoria com o id dado como parâmetro.")
    public ResponseEntity<Void> softDeleteCategory(@PathVariable UUID id) {
        categoryService.softDeleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
