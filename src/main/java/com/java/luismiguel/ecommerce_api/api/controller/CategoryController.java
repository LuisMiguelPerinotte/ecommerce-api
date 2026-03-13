package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.response.GetCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.GetAllActiveCategoriesDTO;
import com.java.luismiguel.ecommerce_api.application.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categoria", description = "Opções de Get das Categorias.")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Obter Todas as Categorias", description = "retorna todas as categorias em paginas e em número de itens escolidos.")
    public ResponseEntity<Page<GetAllActiveCategoriesDTO>> getAllCategories(
            @PageableDefault(size = 20)Pageable pageable
            ) {
        return new ResponseEntity<>(categoryService.getAllCategories(pageable), HttpStatus.OK);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Obter Categoria por ID", description = "retorna uma categoria especifica pelo id dela.")
    public ResponseEntity<GetCategoryResponseDTO> getCategoryById(@PathVariable UUID id) {
        return new ResponseEntity<>(categoryService.getCategoryById(id), HttpStatus.OK);
    }
}
