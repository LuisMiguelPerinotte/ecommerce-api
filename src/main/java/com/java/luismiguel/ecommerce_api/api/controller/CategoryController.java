package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.category.response.GetCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.category.response.GetAllActiveCategoriesDTO;
import com.java.luismiguel.ecommerce_api.application.category.CategoryService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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

import java.util.UUID;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Category retrieval endpoints")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @RateLimiter(name = "public-api")
    @Operation(summary = "Get Categories", description = "Return paginated active categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of active categories", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetAllActiveCategoriesDTO.class)))
    })
    public ResponseEntity<Page<GetAllActiveCategoriesDTO>> getAllCategories(
            @PageableDefault(size = 20)Pageable pageable
            ) {
        return new ResponseEntity<>(categoryService.getAllCategories(pageable), HttpStatus.OK);
    }


    @GetMapping("/{id}")
    @RateLimiter(name = "public-api")
    @Operation(summary = "Get Category by ID", description = "Return a specific category by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    public ResponseEntity<GetCategoryResponseDTO> getCategoryById(@PathVariable UUID id) {
        return new ResponseEntity<>(categoryService.getCategoryById(id), HttpStatus.OK);
    }
}
