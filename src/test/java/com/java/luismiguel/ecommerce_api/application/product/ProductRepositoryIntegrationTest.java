package com.java.luismiguel.ecommerce_api.application.product;

import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import com.java.luismiguel.ecommerce_api.domain.product.LowStockProductProjection;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.product.ProductSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryIntegrationTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should Find Low Stock Products")
    void shouldFindLowStockProducts() {
        // given
        Category category = saveCategory("Eletrônicos", "eletronicos");

        Product lowStockProduct = Product.builder()
                .name("Mouse")
                .description("Mouse gamer")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(3)
                .active(true)
                .category(category)
                .build();

        Product normalStockProduct = Product.builder()
                .name("Teclado")
                .description("Teclado mecânico")
                .price(BigDecimal.valueOf(200))
                .stockQuantity(20)
                .active(true)
                .category(category)
                .build();

        Product inactiveLowStockProduct = Product.builder()
                .name("Headset")
                .description("Headset gamer")
                .price(BigDecimal.valueOf(150))
                .stockQuantity(2)
                .active(false)
                .category(category)
                .build();

        productRepository.save(lowStockProduct);
        productRepository.save(normalStockProduct);
        productRepository.save(inactiveLowStockProduct);

        // when
        List<LowStockProductProjection> result = productRepository.findLowStockProducts(5);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getProductId()).isEqualTo(lowStockProduct.getProductId());
        assertThat(result.getFirst().getName()).isEqualTo("Mouse");
        assertThat(result.getFirst().getStockQuantity()).isEqualTo(3);
    }


    @Test
    @DisplayName("Should Count Low Stock Products")
    void shouldCountLowStockProducts() {
        // given
        Category category = saveCategory("Eletrônicos", "eletronicos");

        Product lowStockProduct = Product.builder()
                .name("Mouse")
                .description("Mouse gamer")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(3)
                .active(true)
                .category(category)
                .build();

        Product anotherLowStockProduct = Product.builder()
                .name("Cabo USB")
                .description("Cabo USB-C")
                .price(BigDecimal.valueOf(30))
                .stockQuantity(4)
                .active(true)
                .category(category)
                .build();

        Product normalStockProduct = Product.builder()
                .name("Teclado")
                .description("Teclado mecânico")
                .price(BigDecimal.valueOf(200))
                .stockQuantity(20)
                .active(true)
                .category(category)
                .build();

        Product inactiveLowStockProduct = Product.builder()
                .name("Headset")
                .description("Headset gamer")
                .price(BigDecimal.valueOf(150))
                .stockQuantity(2)
                .active(false)
                .category(category)
                .build();

        productRepository.save(lowStockProduct);
        productRepository.save(anotherLowStockProduct);
        productRepository.save(normalStockProduct);
        productRepository.save(inactiveLowStockProduct);

        // when
        Integer result = productRepository.countLowStockProducts(5);

        // then
        assertThat(result).isEqualTo(2);
    }


    @Test
    @DisplayName("Should Find Active Products Using Specification")
    void shouldFindActiveProductsUsingSpecification() {
        // given
        Category category = saveCategory("Eletrônicos", "eletronicos");

        Product activeProduct = Product.builder()
                .name("Mouse")
                .description("Mouse gamer")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();

        Product inactiveProduct = Product.builder()
                .name("Teclado")
                .description("Teclado mecânico")
                .price(BigDecimal.valueOf(200))
                .stockQuantity(10)
                .active(false)
                .category(category)
                .build();

        productRepository.save(activeProduct);
        productRepository.save(inactiveProduct);

        // when
        List<Product> result = productRepository.findAll(ProductSpecification.isActive());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getProductId()).isEqualTo(activeProduct.getProductId());
        assertThat(result.getFirst().getActive()).isTrue();
    }


    @Test
    @DisplayName("Should Filter Products By Name Using Specification")
    void shouldFilterProductsByNameUsingSpecification() {
        // given
        Category category = saveCategory("Eletrônicos", "eletronicos");

        Product mouse = Product.builder()
                .name("Mouse Gamer")
                .description("Mouse gamer")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();

        Product keyboard = Product.builder()
                .name("Teclado Mecânico")
                .description("Teclado mecânico")
                .price(BigDecimal.valueOf(200))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();

        productRepository.save(mouse);
        productRepository.save(keyboard);

        Specification<Product> specification = Specification
                .where(ProductSpecification.isActive())
                .and(ProductSpecification.nameContains("mouse"));

        // when
        List<Product> result = productRepository.findAll(specification);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getProductId()).isEqualTo(mouse.getProductId());
        assertThat(result.getFirst().getName()).isEqualTo("Mouse Gamer");
    }


    @Test
    @DisplayName("Should Filter Products By Category Using Specification")
    void shouldFilterProductsByCategoryUsingSpecification() {
        // given
        Category electronics = saveCategory("Eletrônicos", "eletronicos");
        Category clothes = saveCategory("Roupas", "roupas");

        Product mouse = Product.builder()
                .name("Mouse")
                .description("Mouse gamer")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .active(true)
                .category(electronics)
                .build();

        Product shirt = Product.builder()
                .name("Camisa")
                .description("Camisa preta")
                .price(BigDecimal.valueOf(80))
                .stockQuantity(10)
                .active(true)
                .category(clothes)
                .build();

        productRepository.save(mouse);
        productRepository.save(shirt);

        Specification<Product> specification = Specification
                .where(ProductSpecification.isActive())
                .and(ProductSpecification.hasCategory(electronics.getCategoryId()));

        // when
        List<Product> result = productRepository.findAll(specification);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getProductId()).isEqualTo(mouse.getProductId());
        assertThat(result.getFirst().getCategory().getCategoryId()).isEqualTo(electronics.getCategoryId());
    }


    @Test
    @DisplayName("Should Filter Products By Price Range Using Specification")
    void shouldFilterProductsByPriceRangeUsingSpecification() {
        // given
        Category category = saveCategory("Eletrônicos", "eletronicos");

        Product cheapProduct = Product.builder()
                .name("Cabo USB")
                .description("Cabo USB-C")
                .price(BigDecimal.valueOf(30))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();

        Product targetProduct = Product.builder()
                .name("Mouse")
                .description("Mouse gamer")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();

        Product expensiveProduct = Product.builder()
                .name("Monitor")
                .description("Monitor 4K")
                .price(BigDecimal.valueOf(1500))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();

        productRepository.save(cheapProduct);
        productRepository.save(targetProduct);
        productRepository.save(expensiveProduct);

        Specification<Product> specification = Specification
                .where(ProductSpecification.isActive())
                .and(ProductSpecification.priceBetween(BigDecimal.valueOf(50), BigDecimal.valueOf(200)));

        // when
        List<Product> result = productRepository.findAll(specification);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getProductId()).isEqualTo(targetProduct.getProductId());
        assertThat(result.getFirst().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }


    private Category saveCategory(String name, String slug) {
        Category category = Category.builder()
                .name(name)
                .description(name + " description")
                .slug(slug)
                .active(true)
                .build();

        return categoryRepository.save(category);
    }
}