package com.java.luismiguel.ecommerce_api.application.category;

import com.java.luismiguel.ecommerce_api.api.dto.category.request.CreateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.category.request.UpdateCategoryRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.category.response.CreatedCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.category.response.GetAllActiveCategoriesDTO;
import com.java.luismiguel.ecommerce_api.api.dto.category.response.GetCategoryResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.CategoryAlreadyExistsException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.CategoryNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategories {
        UUID categoryId;

        @BeforeEach
        void setUp() {
            categoryId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return Page Of Activated Categories")
        void shouldReturnPageOfActivatedCategories() {
            String name = "category";
            String slug = "category-slug";
            LocalDateTime createdAt = LocalDateTime.now();

            Category category = Category.builder()
                    .name(name)
                    .slug(slug)
                    .categoryId(categoryId)
                    .createdAt(createdAt)
                    .build();

            List<Category> categoriesList = new ArrayList<>(List.of(category));
            Page<Category> categories = new PageImpl<>(categoriesList);
            Pageable pageable = PageRequest.of(0, 10);

            given(categoryRepository.findAllByActiveTrue(any(Pageable.class))).willReturn(categories);

            Page<GetAllActiveCategoriesDTO> result = categoryService.getAllCategories(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);

            GetAllActiveCategoriesDTO dto = result.getContent().getFirst();

            assertThat(dto.categoryId()).isEqualTo(categoryId);
            assertThat(dto.name()).isEqualTo(name);
            assertThat(dto.slug()).isEqualTo(slug);
            assertThat(dto.createdAt()).isEqualTo(createdAt);
        }
    }


    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryById {
        UUID categoryId;

        @BeforeEach
        void setUp() {
            categoryId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Category Not Found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                categoryService.getCategoryById(categoryId);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Inactive Category")
        void shouldThrowExceptionWhenInactiveCategory() {
            Category category = Category.builder()
                    .active(false)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            assertThrows(CategoryNotFoundException.class, () -> {
                categoryService.getCategoryById(categoryId);
            });
        }


        @Test
        @DisplayName("Should Return Category By Id")
        void shouldReturnCategoryById() {
            String name = "category";
            String description = "description";
            String slug = "slug";
            LocalDateTime createdAt = LocalDateTime.now();

            Category category = Category.builder()
                    .categoryId(categoryId)
                    .name(name)
                    .description(description)
                    .slug(slug)
                    .active(true)
                    .createdAt(createdAt)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            GetCategoryResponseDTO result = categoryService.getCategoryById(categoryId);

            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.name()).isEqualTo(name);
            assertThat(result.description()).isEqualTo(description);
            assertThat(result.slug()).isEqualTo(slug);
            assertThat(result.active()).isTrue();
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }
    }


    @Nested
    @DisplayName("softDeleteCategory")
    class SoftDeleteCategory {
        UUID categoryId;

        @BeforeEach
        void setUp() {
            categoryId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Category Not Found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                categoryService.softDeleteCategory(categoryId);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Inactive Category")
        void shouldThrowExceptionWhenInactiveCategory() {
            Category category = Category.builder()
                    .active(false)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            assertThrows(CategoryNotFoundException.class, () -> {
                categoryService.softDeleteCategory(categoryId);
            });
        }


        @Test
        @DisplayName("Should Soft Delete Category Successfully")
        void shouldSoftDeleteCategorySuccessfully() {
            Category category = Category.builder()
                    .active(true)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            categoryService.softDeleteCategory(categoryId);

            assertThat(category.getActive()).isFalse();
            then(categoryRepository).should().save(category);
        }
    }


    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {
        UUID categoryId;
        UpdateCategoryRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            categoryId = UUID.randomUUID();
            requestDTO = new UpdateCategoryRequestDTO("name", "description");
        }

        @Test
        @DisplayName("Should Throw Exception When Category Not Found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                categoryService.updateCategory(categoryId, requestDTO);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Inactive Category")
        void shouldThrowExceptionWhenInactiveCategory() {
            Category category = Category.builder()
                    .active(false)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            assertThrows(CategoryNotFoundException.class, () -> {
                categoryService.updateCategory(categoryId, requestDTO);
            });
        }


        @Test
        @DisplayName("Should Update Category Successfully")
        void shouldUpdateCategorySuccessfully() {
            UpdateCategoryRequestDTO updateRequestDTO = new UpdateCategoryRequestDTO(
                "newCategoryName", "NewCategoryDescription"
            );

            Category category = Category.builder()
                    .name("oldCategoryName")
                    .description("oldCategoryDescription")
                    .active(true)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            categoryService.updateCategory(categoryId, updateRequestDTO);

            assertThat(updateRequestDTO.name().toLowerCase()).isEqualTo(category.getName());
            assertThat(updateRequestDTO.description()).isEqualTo(category.getDescription());

            then(categoryRepository).should().save(category);
        }


        @Test
        @DisplayName("Should Update Category Name Successfully")
        void shouldUpdateCategoryNameSuccessfully() {
            UpdateCategoryRequestDTO updateRequestDTO = new UpdateCategoryRequestDTO(
                    "newCategoryName",
                    null
            );

            Category category = Category.builder()
                    .name("oldCategoryName")
                    .active(true)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            categoryService.updateCategory(categoryId, updateRequestDTO);

            assertThat(updateRequestDTO.name().toLowerCase()).isEqualTo(category.getName());

            then(categoryRepository).should().save(category);
        }


        @Test
        @DisplayName("should Update Category Description Successfully")
        void shouldUpdateCategoryDescriptionSuccessfully() {
            UpdateCategoryRequestDTO updateRequestDTO = new UpdateCategoryRequestDTO(
                    null,
                    "newCategoryDescription"
            );

            Category category = Category.builder()
                    .description("oldCategoryDescription")
                    .active(true)
                    .build();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            categoryService.updateCategory(categoryId, updateRequestDTO);

            assertThat(updateRequestDTO.description()).isEqualTo(category.getDescription());

            then(categoryRepository).should().save(category);
        }
    }


    @Nested
    @DisplayName("createCategory")
    class CreateCategory {
        UUID categoryId;
        CreateCategoryRequestDTO requestDTO;
        String categoryName;
        String categorySlug;
        LocalDateTime createdAt;

        @BeforeEach
        void setUp() {
            categoryId = UUID.randomUUID();
            requestDTO = new CreateCategoryRequestDTO("category-name", "category-description");
            categoryName = "category-name";
            categorySlug = "category-slug";
            createdAt = LocalDateTime.now();
        }

        @Test
        @DisplayName("Should Throw Exception When Category Already Exists")
        void shouldThrowExceptionWhenCategoryAlreadyExists() {
            Category category = Category.builder()
                    .name("category-name")
                    .active(true)
                    .build();

            given(categoryRepository.findByName(requestDTO.name())).willReturn(Optional.of(category));

            assertThrows(CategoryAlreadyExistsException.class, () -> {
                categoryService.createCategory(requestDTO);
            });
        }


        @Test
        @DisplayName("Should Reactivate Existent Inactive Category")
        void shouldReactivateExistentInactiveCategory() {
            Category category = Category.builder()
                    .categoryId(categoryId)
                    .name(categoryName)
                    .description("old-category-description")
                    .slug(categorySlug)
                    .createdAt(createdAt)
                    .active(false)
                    .build();

            Category savedCategory = Category.builder()
                    .categoryId(categoryId)
                    .name(categoryName)
                    .description(requestDTO.description())
                    .slug(categorySlug)
                    .createdAt(createdAt)
                    .active(true)
                    .build();

            given(categoryRepository.findByName(requestDTO.name())).willReturn(Optional.of(category));
            given(categoryRepository.save(category)).willReturn(savedCategory);

            CreatedCategoryResponseDTO result = categoryService.createCategory(requestDTO);

            assertThat(result.name()).isEqualTo(categoryName);
            assertThat(result.description()).isEqualTo(requestDTO.description());
            assertThat(result.slug()).isEqualTo(categorySlug);
            assertThat(result.active()).isTrue();
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }


        @Test
        @DisplayName("Should Create New Category Successfully")
        void shouldCreateNewCategorySuccessfully() {
            Category savedCategory = Category.builder()
                    .categoryId(categoryId)
                    .name(requestDTO.name())
                    .description(requestDTO.description())
                    .slug(categorySlug)
                    .active(true)
                    .createdAt(createdAt)
                    .build();

            given(categoryRepository.findByName(requestDTO.name())).willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class))).willReturn(savedCategory);

            CreatedCategoryResponseDTO result = categoryService.createCategory(requestDTO);

            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.name()).isEqualTo(requestDTO.name());
            assertThat(result.description()).isEqualTo(requestDTO.description());
            assertThat(result.slug()).isEqualTo(categorySlug);
            assertThat(result.active()).isTrue();
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }
    }
}
