package com.java.luismiguel.ecommerce_api.application.category;

import com.java.luismiguel.ecommerce_api.api.dto.request.CreateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.UpdateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.GetCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.CreatedCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.GetAllActiveCategoriesDTO;
import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.CategoryAlreadyExistsException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.CategoryNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Page<GetAllActiveCategoriesDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAllByActiveTrue(pageable)
                .map(category -> new GetAllActiveCategoriesDTO(
                        category.getCategoryId(),
                        category.getName(),
                        category.getSlug(),
                        category.getCreatedAt()
                ));
    }


    public GetCategoryResponseDTO getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        if (!category.getActive()) {
            throw new CategoryNotFoundException();
        }

        return new GetCategoryResponseDTO(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getSlug(),
                category.getActive(),
                category.getCreatedAt()
        );
    }


    public CreatedCategoryResponseDTO createCategory(CreateCategoryRequestDTO createCategoryRequestDTO) {
        Optional<Category> existing = categoryRepository.findByName(createCategoryRequestDTO.name().trim().toLowerCase());
        Category category;

        if (existing.isPresent()) {
            category = existing.get();

            if (category.getActive()) {
                throw new CategoryAlreadyExistsException();
            }

            category.setActive(Boolean.TRUE);
            category.setDescription(createCategoryRequestDTO.description());

        } else {
            category = Category.builder()
                    .name(createCategoryRequestDTO.name().toLowerCase().trim())
                    .description(createCategoryRequestDTO.description())
                    .slug(toSlug(createCategoryRequestDTO.name()))
                    .active(Boolean.TRUE)
                    .build();

        }
        return toDTO(categoryRepository.save(category));
    }


    public void updateCategory(UUID id, UpdateCategoryRequestDTO updateCategoryRequestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        if (!category.getActive()) {
            throw new CategoryNotFoundException();
        }

        category.setName(updateCategoryRequestDTO.name().toLowerCase().trim());
        category.setDescription(updateCategoryRequestDTO.description().trim());

        categoryRepository.save(category);
    }


    public void softDeleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        if (!category.getActive()) {
            throw new CategoryNotFoundException();
        }

        category.setActive(Boolean.FALSE);
        categoryRepository.save(category);
    }


    // Private Methods //
    private CreatedCategoryResponseDTO toDTO(Category savedCategory) {
        return new CreatedCategoryResponseDTO(
                savedCategory.getCategoryId(),
                savedCategory.getName(),
                savedCategory.getDescription(),
                savedCategory.getSlug(),
                savedCategory.getActive(),
                savedCategory.getCreatedAt()
        );
    }


    private static String toSlug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replace("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }
}
