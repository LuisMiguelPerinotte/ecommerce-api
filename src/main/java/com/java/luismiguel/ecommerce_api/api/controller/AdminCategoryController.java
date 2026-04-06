package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.category.request.CreateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.category.request.UpdateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.category.response.CreatedCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.application.category.CategoryService;
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
@RequestMapping("/admin/categories")
@Tag(name = "Admin Categories", description = "APIs for administrators to manage categories, including creation, update and deletion.")
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Create Category", description = "Create a new category with the provided information.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreatedCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid category data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Category already exists (conflict)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content)
    })
    public ResponseEntity<CreatedCategoryResponseDTO> createCategory(
            @Valid
            @RequestBody CreateCategoryRequestDTO createCategoryRequestDTO
    ) {
        return new ResponseEntity<>(categoryService.createCategory(createCategoryRequestDTO), HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Update Category", description = "Update an existing category by the provided id.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category updated (no content)"),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid category fields", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
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
    @RateLimiter(name = "critical")
    @Operation(summary = "Soft Delete Category", description = "Soft delete a category by id (marks as inactive).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category soft-deleted (no content)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    public ResponseEntity<Void> softDeleteCategory(@PathVariable UUID id) {
        categoryService.softDeleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
