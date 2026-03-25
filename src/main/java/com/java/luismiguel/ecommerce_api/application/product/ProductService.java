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
import com.java.luismiguel.ecommerce_api.domain.product.ProductSpecification;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<GetAllProductsResponseDTO> getAllProducts(
            String name,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {

        Specification<Product> specification = Specification
                .where(ProductSpecification.isActive())
                .and(ProductSpecification.nameContains(name))
                .and(ProductSpecification.hasCategory(categoryId))
                .and(ProductSpecification.priceBetween(minPrice, maxPrice));

        return productRepository.findAll(specification, pageable)
                .map(product -> new GetAllProductsResponseDTO(
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getCategory().getName()
                ));
    }


    public GetProductResponseDTO getProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        return new GetProductResponseDTO(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }


    public CreatedProductResponseDTO createProduct(CreateProductRequestDTO createProductRequestDTO) {
        Category category = categoryRepository.findById(createProductRequestDTO.categoryId())
                .orElseThrow(CategoryNotFoundException::new);

        Product product = Product.builder()
                .name(createProductRequestDTO.name())
                .description(createProductRequestDTO.description())
                .price(createProductRequestDTO.price())
                .stockQuantity(createProductRequestDTO.stockQuantity())
                .active(Boolean.TRUE)
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);

        return new CreatedProductResponseDTO(
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getCreatedAt()
        );
    }


    public void updateProduct(UUID id, UpdateProductRequestDTO updateProductRequestDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getActive()) {
            throw new ProductNotFoundException();
        }

        Optional.ofNullable(updateProductRequestDTO.name())
                .map(String::trim)
                .ifPresent(product::setName);

        Optional.ofNullable(updateProductRequestDTO.description())
                .map(String::trim)
                .ifPresent(product::setDescription);

        Optional.ofNullable(updateProductRequestDTO.price())
                .ifPresent(product::setPrice);

        Optional.ofNullable(updateProductRequestDTO.categoryId())
                .ifPresent(categoryId -> {
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(CategoryAlreadyExistsException::new);
                    product.setCategory(category);
                });

        productRepository.save(product);
    }


    public void adjustStock(UUID id, AdjustProductStockRequestDTO adjustProductStockDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getActive()) {
            throw new ProductNotFoundException();
        }

        product.setStockQuantity(adjustProductStockDTO.quantity());
        productRepository.save(product);
    }


    public void softDeleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getActive()) {
            throw new ProductNotFoundException();
        }

        product.setActive(Boolean.FALSE);
        productRepository.save(product);
    }


    public void activateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (product.getActive()) {
            throw new ProductAlreadyActivatedException();
        }

        product.setActive(Boolean.TRUE);
        productRepository.save(product);
    }


    public void deactivateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getActive()) {
            throw new ProductAlreadyDeactivatedException();
        }

        product.setActive(Boolean.FALSE);
        productRepository.save(product);
    }
}
