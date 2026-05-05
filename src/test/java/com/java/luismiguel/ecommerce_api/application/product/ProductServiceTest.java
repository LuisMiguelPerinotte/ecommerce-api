package com.java.luismiguel.ecommerce_api.application.product;

import com.java.luismiguel.ecommerce_api.api.dto.product.request.AdjustProductStockRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.request.CreateProductRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.request.UpdateProductRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.response.CreatedProductResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.response.GetAllProductsResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.product.response.GetProductResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.CategoryNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.ProductAlreadyActivatedException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.ProductAlreadyDeactivatedException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.ProductNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Nested
    @DisplayName("getProductById")
    class GetProductById {
        UUID productId;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Product Not Found")
        void shouldThrowExceptionWhenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> {
                productService.getProductById(productId);
            });
        }


        @Test
        @DisplayName("Should Return Product By Id Successfully")
        void shouldReturnProductByIdSuccessfully() {
            String productName = "productName";
            String productDescription = "productDescription";
            BigDecimal price = BigDecimal.TEN;
            Integer stockQuantity = 1;
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime updatedAt = LocalDateTime.now();

            Category category = Category.builder()
                    .name("category")
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name(productName)
                    .description(productDescription)
                    .price(price)
                    .stockQuantity(stockQuantity)
                    .category(category)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            GetProductResponseDTO result = productService.getProductById(productId);

            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.name()).isEqualTo(productName);
            assertThat(result.description()).isEqualTo(productDescription);
            assertThat(result.price()).isEqualByComparingTo(price);
            assertThat(result.stockQuantity()).isEqualTo(stockQuantity);
            assertThat(result.categoryName()).isEqualTo(category.getName());
            assertThat(result.createdAt()).isEqualTo(createdAt);
            assertThat(result.updatedAt()).isEqualTo(updatedAt);
        }
    }


    @Nested
    @DisplayName("activateProduct")
    class ActivateProduct {
        UUID productId;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Product Not Found")
        void shouldThrowExceptionWhenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> {
                productService.activateProduct(productId);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Product Is Already Activated")
        void shouldThrowExceptionWhenProductIsAlreadyActivated() {
            Product product = Product.builder()
                    .active(true)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            assertThrows(ProductAlreadyActivatedException.class, () -> {
                productService.activateProduct(productId);
            });
        }


        @Test
        @DisplayName("Should Activate Product Successfully")
        void shouldActivateProductSuccessfully() {
            Product product = Product.builder()
                    .active(false)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            productService.activateProduct(productId);

            then(productRepository).should().save(product);
        }
    }


    @Nested
    @DisplayName("deactivateProduct")
    class DeactivateProduct {
        UUID productId;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Product Not Found")
        void shouldThrowExceptionWhenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> {
                productService.deactivateProduct(productId);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Product Is Already Deactivated")
        void shouldThrowExceptionWhenProductIsAlreadyDeactivated() {
            Product product = Product.builder()
                    .active(false)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            assertThrows(ProductAlreadyDeactivatedException.class, () -> {
                productService.deactivateProduct(productId);
            });
        }


        @Test
        @DisplayName("Should Deactivate Product Successfully")
        void shouldDeactivateProductSuccessfully() {
            Product product = Product.builder()
                    .active(true)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            productService.deactivateProduct(productId);

            then(productRepository).should().save(product);
        }
    }


    @Nested
    @DisplayName("adjustStock")
    class AdjustStock {
        UUID productId;
        AdjustProductStockRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
            requestDTO = new AdjustProductStockRequestDTO(10);
        }


        @Test
        @DisplayName("Should Throw Exception When Product Not Found")
        void shouldThrowExceptionWhenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> {
                productService.adjustStock(productId, requestDTO);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Product Inactive")
        void shouldThrowExceptionWhenProductInactive() {
            Product product = Product.builder()
                    .active(false)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            assertThrows(ProductNotFoundException.class, () -> {
                productService.adjustStock(productId, requestDTO);
            });
        }


        @Test
        @DisplayName("Should Update Stock Quantity Successfully")
        void shouldUpdateStockQuantitySuccessfully() {
            Product product = Product.builder()
                    .active(true)
                    .stockQuantity(2)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            productService.adjustStock(productId, requestDTO);

            then(productRepository).should().save(product);
        }
    }


    @Nested
    @DisplayName("createProduct")
    class CreateProduct {
        UUID productId;
        UUID categoryId;
        String name;
        String description ;
        BigDecimal price;
        Integer quantity;
        CreateProductRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
            categoryId = UUID.randomUUID();
            name = "productName";
            description = "productDescription";
            price  = BigDecimal.TEN;
            quantity = 10;

            requestDTO = new CreateProductRequestDTO(
                    name,
                    description,
                    price,
                    quantity,
                    categoryId
            );
        }


        @Test
        @DisplayName("Should Throw Exception When Category Not Found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            given(categoryRepository.findById(requestDTO.categoryId())).willReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                productService.createProduct(requestDTO);
            });
        }


        @Test
        @DisplayName("Should Create Product Successfully")
        void shouldCreateProductSuccessfully() {
            LocalDateTime createdAt = LocalDateTime.now();

            Category category = Category.builder()
                    .name("category")
                    .build();

            Product savedProduct = Product.builder()
                    .productId(productId)
                    .name(requestDTO.name())
                    .description(requestDTO.description())
                    .price(requestDTO.price())
                    .createdAt(createdAt)
                    .build();

            given(categoryRepository.findById(requestDTO.categoryId())).willReturn(Optional.of(category));
            given(productRepository.save(any(Product.class))).willReturn(savedProduct);

            CreatedProductResponseDTO result = productService.createProduct(requestDTO);

            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.name()).isEqualTo(requestDTO.name());
            assertThat(result.description()).isEqualTo(requestDTO.description());
            assertThat(result.price()).isEqualByComparingTo(requestDTO.price());
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }
    }


    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {
        UUID productId;
        UUID categoryId;
        String name;
        String description ;
        BigDecimal price;
        UpdateProductRequestDTO updateRequestDTO;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
            categoryId = UUID.randomUUID();
            name = "newName";
            description = "newDescription";
            price  = BigDecimal.TEN;

            updateRequestDTO = new UpdateProductRequestDTO(
                    name,
                    description,
                    price,
                    categoryId
            );
        }

        @Test
        @DisplayName("Should Throw Exception When Product Not Found")
        void shouldThrowExceptionWhenProductNotFound() {
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> {
                productService.updateProduct(productId, updateRequestDTO);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Product Inactive")
        void shouldThrowExceptionWhenProductInactive() {
            Product product = Product.builder()
                    .active(false)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            assertThrows(ProductNotFoundException.class, () -> {
                productService.updateProduct(productId, updateRequestDTO);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Category Not Found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            UpdateProductRequestDTO request = new UpdateProductRequestDTO(
                    null,
                    null,
                    null,
                    UUID.randomUUID()
            );

            Product product = Product.builder()
                    .active(true)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                productService.updateProduct(productId, request);
            });
        }


        @Test
        @DisplayName("Should Update All Fields Successfully")
        void shouldUpdateAllFieldsSuccessfully() {
            Category oldCategory = Category.builder()
                    .name("oldCategory")
                    .build();

            Product product = Product.builder()
                    .name("oldName")
                    .description("oldDescription")
                    .price(BigDecimal.ONE)
                    .category(oldCategory)
                    .active(true)
                    .build();

            Category newCategory = Category.builder()
                    .name("newCategory")
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(categoryRepository.findById(updateRequestDTO.categoryId())).willReturn(Optional.of(newCategory));

            productService.updateProduct(productId, updateRequestDTO);

            assertThat(product.getName()).isEqualTo(updateRequestDTO.name());
            assertThat(product.getDescription()).isEqualTo(updateRequestDTO.description());
            assertThat(product.getPrice()).isEqualByComparingTo(updateRequestDTO.price());
            assertThat(product.getCategory().getName()).isEqualTo(newCategory.getName());

            then(productRepository).should().save(product);
        }


        @Test
        @DisplayName("Should Update Only Name When Another Fields Are Null")
        void shouldUpdateOnlyNameWhenAnotherFieldsAreNull() {
            UpdateProductRequestDTO request = new UpdateProductRequestDTO(
                    "newName",
                    null,
                    null,
                    null
            );

            Category oldCategory = Category.builder()
                    .name("oldCategory")
                    .build();

            Product product = Product.builder()
                    .name("oldName")
                    .description("oldDescription")
                    .price(BigDecimal.ONE)
                    .category(oldCategory)
                    .active(true)
                    .build();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            productService.updateProduct(productId, request);

            assertThat(product.getName()).isEqualTo(request.name());
            assertThat(product.getDescription()).isEqualTo("oldDescription");
            assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(product.getCategory().getName()).isEqualTo("oldCategory");
        }
    }


    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {
        UUID productId;
        UUID categoryId;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
            categoryId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return All Products Without Filters Successfully")
        void shouldReturnAllProductsWithoutFiltersSuccessfully() {
            Category category = Category.builder()
                    .categoryId(categoryId)
                    .name("category")
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("product")
                    .price(BigDecimal.valueOf(1000))
                    .category(category)
                    .active(true)
                    .build();

            Page<Product> productsPage = new PageImpl<>(List.of(product));
            Pageable pageable = PageRequest.of(0, 10);

            given(productRepository.findAll(any(Specification.class), eq(pageable))).willReturn(productsPage);

            Page<GetAllProductsResponseDTO> result = productService.getAllProducts(
                    null,
                    null,
                    null,
                    null,
                    pageable
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);

            GetAllProductsResponseDTO dto = result.getContent().getFirst();

            assertThat(dto.productId()).isEqualTo(productId);
            assertThat(dto.name()).isEqualTo("product");
            assertThat(dto.price()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(dto.categoryName()).isEqualTo("category");
        }


        @Test
        @DisplayName("Should Filter Products By Name")
        void shouldFilterProductsByName() {
            Category category = Category.builder()
                    .categoryId(categoryId)
                    .name("category")
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("100 Product")
                    .price(BigDecimal.valueOf(1000))
                    .category(category)
                    .active(true)
                    .build();

            Page<Product> productsPage = new PageImpl<>(List.of(product));
            Pageable pageable = PageRequest.of(0, 10);

            given(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(productsPage);

            Page<GetAllProductsResponseDTO> result = productService.getAllProducts(
                    "product",
                    null,
                    null,
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().name()).isEqualTo("100 Product");
        }


        @Test
        @DisplayName("Should Filter Products By Category")
        void shouldFilterProductsByCategory() {
            Category category = Category.builder()
                    .categoryId(categoryId)
                    .name("category")
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("product")
                    .price(BigDecimal.valueOf(1000))
                    .category(category)
                    .active(true)
                    .build();

            Page<Product> productsPage = new PageImpl<>(List.of(product));
            Pageable pageable = PageRequest.of(0, 10);

            given(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(productsPage);

            Page<GetAllProductsResponseDTO> result = productService.getAllProducts(
                    null,
                    categoryId,
                    null,
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().categoryName()).isEqualTo("category");
        }


        @Test
        @DisplayName("Should Filter Products By Price Range")
        void shouldFilterProductsByPriceRange() {
            Category category = Category.builder()
                    .categoryId(categoryId)
                    .name("category")
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("product")
                    .price(BigDecimal.valueOf(1000))
                    .category(category)
                    .active(true)
                    .build();

            Page<Product> productsPage = new PageImpl<>(List.of(product));
            Pageable pageable = PageRequest.of(0, 10);

            given(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(productsPage);

            Page<GetAllProductsResponseDTO> result = productService.getAllProducts(
                    null,
                    null,
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(1500),
                    pageable
            );

            assertThat(result.getContent()).hasSize(1);
            GetAllProductsResponseDTO dto = result.getContent().getFirst();

            assertThat(dto.price()).isGreaterThanOrEqualTo(BigDecimal.valueOf(500));
            assertThat(dto.price()).isLessThanOrEqualTo(BigDecimal.valueOf(1500));
        }


        @Test
        @DisplayName("Should Combine Multiple Filters")
        void shouldCombineMultipleFilters() {
            Category category = Category.builder()
                    .categoryId(categoryId)
                    .name("category")
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("100 Product")
                    .price(BigDecimal.valueOf(1000))
                    .category(category)
                    .active(true)
                    .build();

            Page<Product> productsPage = new PageImpl<>(List.of(product));
            Pageable pageable = PageRequest.of(0, 10);

            given(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(productsPage);

            Page<GetAllProductsResponseDTO> result = productService.getAllProducts(
                    "product",
                    categoryId,
                    BigDecimal.valueOf(500),
                    BigDecimal.valueOf(1500),
                    pageable
            );

            GetAllProductsResponseDTO dto = result.getContent().getFirst();

            assertThat(dto.name()).containsIgnoringCase("product");
            assertThat(dto.categoryName()).isEqualTo("category");
            assertThat(dto.price()).isGreaterThanOrEqualTo(BigDecimal.valueOf(1000));
        }
    }
}
