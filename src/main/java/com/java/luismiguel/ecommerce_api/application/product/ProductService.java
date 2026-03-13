package com.java.luismiguel.ecommerce_api.application.product;

import com.java.luismiguel.ecommerce_api.api.dto.response.GetAllProductsResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.GetProductResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.product.ProductSpecification;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.ProductNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
}
