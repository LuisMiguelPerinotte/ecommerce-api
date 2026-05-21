package com.java.luismiguel.ecommerce_api.application.cart;

import com.java.luismiguel.ecommerce_api.domain.cart.*;
import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CartRepositoryIntegrationTest {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should Find Cart By User Id")
    void shouldFindCartByUserId() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        Cart cart = Cart.builder()
                .user(user)
                .items(List.of())
                .build();

        Cart savedCart = cartRepository.save(cart);

        // when
        Cart result = cartRepository.findByUserUserId(user.getUserId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(savedCart.getCartId());
        assertThat(result.getUser().getUserId()).isEqualTo(user.getUserId());
    }


    @Test
    @DisplayName("Should Find Cart By User Id With Items")
    void shouldFindCartByUserIdWithItems() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        Cart cart = Cart.builder()
                .user(user)
                .build();

        Cart savedCart = cartRepository.save(cart);

        CartItem cartItem = CartItem.builder()
                .cart(savedCart)
                .product(product)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100))
                .subtotal(BigDecimal.valueOf(200))
                .build();

        savedCart.setItems(new ArrayList<>(List.of(cartItem)));
        cartRepository.save(savedCart);

        // when
        Optional<Cart> result = cartRepository.findByUserUserIdWithItems(user.getUserId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCartId()).isEqualTo(savedCart.getCartId());
        assertThat(result.get().getItems()).hasSize(1);
        assertThat(result.get().getItems().getFirst().getProduct().getProductId()).isEqualTo(product.getProductId());
        assertThat(result.get().getItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(result.get().getItems().getFirst().getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }


    @Test
    @DisplayName("Should Return Empty When Cart With Items Does Not Exist")
    void shouldReturnEmptyWhenCartWithItemsDoesNotExist() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        // when
        Optional<Cart> result = cartRepository.findByUserUserIdWithItems(user.getUserId());

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("Should Return Cart Summary")
    void shouldReturnCartSummary() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Product mouse = saveProduct("Mouse", BigDecimal.valueOf(100));
        Product keyboard = saveProduct("Teclado", BigDecimal.valueOf(200));

        Cart cart = Cart.builder()
                .user(user)
                .build();

        Cart savedCart = cartRepository.save(cart);

        CartItem firstItem = CartItem.builder()
                .cart(savedCart)
                .product(mouse)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100))
                .subtotal(BigDecimal.valueOf(200))
                .build();

        CartItem secondItem = CartItem.builder()
                .cart(savedCart)
                .product(keyboard)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(200))
                .subtotal(BigDecimal.valueOf(200))
                .build();

        savedCart.setItems(new ArrayList<>(List.of(firstItem, secondItem)));
        cartRepository.save(savedCart);

        // when
        CartSummary result = cartRepository.getCartSummary(savedCart.getCartId());

        // then
        assertThat(result.getTotalItems()).isEqualTo(3);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }


    @Test
    @DisplayName("Should Return Null Summary Values When Cart Has No Items")
    void shouldReturnNullSummaryValuesWhenCartHasNoItems() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        Cart cart = Cart.builder()
                .user(user)
                .items(List.of())
                .build();

        Cart savedCart = cartRepository.save(cart);

        // when
        CartSummary result = cartRepository.getCartSummary(savedCart.getCartId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isNull();
        assertThat(result.getTotalAmount()).isNull();
    }


    @Test
    @DisplayName("Should Delete All Cart Items By Cart Id")
    void shouldDeleteAllCartItemsByCartId() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        Cart cart = Cart.builder()
                .user(user)
                .build();

        Cart savedCart = cartRepository.save(cart);

        CartItem firstItem = CartItem.builder()
                .cart(savedCart)
                .product(product)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100))
                .subtotal(BigDecimal.valueOf(200))
                .build();

        CartItem secondItem = CartItem.builder()
                .cart(savedCart)
                .product(product)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100))
                .subtotal(BigDecimal.valueOf(100))
                .build();

        savedCart.setItems(new ArrayList<>(List.of(firstItem, secondItem)));
        cartRepository.save(savedCart);

        assertThat(cartItemRepository.findAll()).hasSize(2);

        // when
        cartItemRepository.deleteAllByCartId(savedCart.getCartId());
        cartRepository.flush();

        // then
        assertThat(cartItemRepository.findAll()).isEmpty();
    }


    private User saveUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password("encoded-password")
                .userRole(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        return userRepository.save(user);
    }


    private Category saveCategory() {
        Category category = Category.builder()
                .name("Eletrônicos")
                .description("Produtos eletrônicos")
                .slug("eletronicos")
                .active(true)
                .build();

        return categoryRepository.save(category);
    }


    private Product saveProduct(String name, BigDecimal price) {
        Category category = Category.builder()
                .name("Categoria " + name)
                .description("Categoria de " + name)
                .slug("categoria-" + name.toLowerCase())
                .active(true)
                .build();

        Category savedCategory = categoryRepository.save(category);

        Product product = Product.builder()
                .name(name)
                .description(name + " description")
                .price(price)
                .stockQuantity(10)
                .imageUrl("https://image.com/" + name)
                .sku("SKU-" + name)
                .active(true)
                .category(savedCategory)
                .build();

        return productRepository.save(product);
    }
}