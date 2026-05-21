package com.java.luismiguel.ecommerce_api.application.category;

import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CategoryRepositoryIntegrationTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should Find Category By Name")
    void shouldFindCategoryByName() {
        // given
        Category category = Category.builder()
                .name("Eletrônicos")
                .description("Produtos eletrônicos")
                .slug("eletronicos")
                .active(true)
                .build();

        Category savedCategory = categoryRepository.save(category);

        // when
        Optional<Category> result = categoryRepository.findByName("Eletrônicos");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(savedCategory.getCategoryId());
        assertThat(result.get().getName()).isEqualTo("Eletrônicos");
        assertThat(result.get().getDescription()).isEqualTo("Produtos eletrônicos");
        assertThat(result.get().getSlug()).isEqualTo("eletronicos");
        assertThat(result.get().getActive()).isTrue();
        assertThat(result.get().getCreatedAt()).isNotNull();
    }


    @Test
    @DisplayName("Should Return Empty When Category Name Does Not Exist")
    void shouldReturnEmptyWhenCategoryNameDoesNotExist() {
        // when
        Optional<Category> result = categoryRepository.findByName("Categoria inexistente");

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("Should Find All Active Categories")
    void shouldFindAllActiveCategories() {
        // given
        Category activeCategory = Category.builder()
                .name("Eletrônicos")
                .description("Produtos eletrônicos")
                .slug("eletronicos")
                .active(true)
                .build();

        Category inactiveCategory = Category.builder()
                .name("Roupas")
                .description("Roupas e acessórios")
                .slug("roupas")
                .active(false)
                .build();

        categoryRepository.save(activeCategory);
        categoryRepository.save(inactiveCategory);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Category> result = categoryRepository.findAllByActiveTrue(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Eletrônicos");
        assertThat(result.getContent().getFirst().getActive()).isTrue();
    }


    @Test
    @DisplayName("Should Not Return Inactive Categories")
    void shouldNotReturnInactiveCategories() {
        // given
        Category inactiveCategory = Category.builder()
                .name("Roupas")
                .description("Roupas e acessórios")
                .slug("roupas")
                .active(false)
                .build();

        categoryRepository.save(inactiveCategory);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Category> result = categoryRepository.findAllByActiveTrue(pageable);

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}