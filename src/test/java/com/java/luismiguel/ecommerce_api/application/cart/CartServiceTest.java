package com.java.luismiguel.ecommerce_api.application.cart;

import com.java.luismiguel.ecommerce_api.api.dto.cart.request.AddCartItemRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.request.UpdateCartItemQuantityRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.response.AddedCartItemDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.response.GetCartResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.cart.response.ListCartItemsResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.cart.*;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart.CartIsEmptyException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart.CartItemNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.InsufficientProductStockException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.ProductNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @InjectMocks
    CartService cartService;

    @Mock
    CartRepository cartRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Nested
    @DisplayName("getCart")
    class GetCart {
        UUID userId;
        UUID cartId;
        UUID productId;
        UUID cartItemId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            cartId = UUID.randomUUID();
            productId = UUID.randomUUID();
            cartItemId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return Cart With Items")
        void shouldReturnCartWithItems() {
            // given
            Product product = Product.builder()
                    .productId(productId)
                    .name("Test Product")
                    .build();

            CartItem cartItem = CartItem.builder()
                    .cartItemId(cartItemId)
                    .product(product)
                    .unitPrice(BigDecimal.TEN)
                    .quantity(2)
                    .subtotal(BigDecimal.valueOf(20))
                    .build();

            Cart cart = Cart.builder()
                    .cartId(cartId)
                    .items(new ArrayList<>(List.of(cartItem)))
                    .build();

            CartSummary summary = new CartSummary() {
                @Override
                public Integer getTotalItems() {
                    return 2;
                }

                @Override
                public BigDecimal getTotalAmount() {
                    return BigDecimal.valueOf(20);
                }
            };

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(cartRepository.getCartSummary(cartId)).willReturn(summary);

            // when
            GetCartResponseDTO result = cartService.getCart(userId);

            // then
            assertThat(result.cartId()).isEqualTo(cartId);
            assertThat(result.items()).hasSize(1);
            assertThat(result.totalItems()).isEqualTo(2);
            assertThat(result.totalAmount()).isEqualTo(BigDecimal.valueOf(20));

            ListCartItemsResponseDTO item = result.items().getFirst();
            assertThat(item.cartItemId()).isEqualTo(cartItemId);
            assertThat(item.productId()).isEqualTo(productId);
            assertThat(item.productName()).isEqualTo("Test Product");
        }


        @Test
        @DisplayName("Should Return Empty Cart")
        void shouldReturnEmptyCart() {
            // given
            Cart cart = Cart.builder()
                    .cartId(cartId)
                    .items(new ArrayList<>())
                    .build();

            CartSummary summary = new CartSummary() {
                @Override
                public Integer getTotalItems() {
                    return 0;
                }

                @Override
                public BigDecimal getTotalAmount() {
                    return BigDecimal.ZERO;
                }
            };

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(cartRepository.getCartSummary(cartId)).willReturn(summary);

            // when
            GetCartResponseDTO result = cartService.getCart(userId);

            // then
            assertThat(result.cartId()).isEqualTo(cartId);
            assertThat(result.items()).isEmpty();
            assertThat(result.totalItems()).isZero();
            assertThat(result.totalAmount()).isEqualTo(BigDecimal.ZERO);
        }
    }


    @Nested
    @DisplayName("addCartItem")
    class AddCartItem {
        UUID userId;
        UUID productId;
        AddCartItemRequestDTO addCartItemRequestDTO;
        Cart cart;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            productId = UUID.randomUUID();

            addCartItemRequestDTO = new AddCartItemRequestDTO(productId, 1);
            cart = new Cart();
        }


        @Test
        @DisplayName("Should Throw Exception When Product Not Found")
        void shouldThrowExceptionWhenProductNotFound() {
            // given
            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            // when + then
            ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
                cartService.addCartItem(addCartItemRequestDTO, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Product Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Product Is Not Active")
        void shouldThrowExceptionWhenProductIsNotActive() {
            // given
            Product product = Product.builder()
                    .productId(productId)
                    .active(false)
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            // when + then
            ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
                cartService.addCartItem(addCartItemRequestDTO, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Product Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Product Has Insufficient Stock")
        void shouldThrowExceptionWhenProductHasInsufficientStock() {
            // given
            String productName = "productName";

            Product product = Product.builder()
                    .name(productName)
                    .active(Boolean.TRUE)
                    .stockQuantity(0)
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            // when + then
            InsufficientProductStockException exception = assertThrows(InsufficientProductStockException.class, () -> {
                cartService.addCartItem(addCartItemRequestDTO, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Product " + productName + " has Insufficient Stock!");
        }


        @Test
        @DisplayName("Should Throw Exception When Existing Cart Item Quantity Exceeds Product Stock")
        void shouldThrowExceptionWhenExistingCartItemQuantityExceedsProductStock() {
            // given
            addCartItemRequestDTO = new AddCartItemRequestDTO(productId, 3);

            String productName = "productName";

            Product product = Product.builder()
                    .productId(productId)
                    .name(productName)
                    .price(BigDecimal.TEN)
                    .active(Boolean.TRUE)
                    .stockQuantity(5)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .quantity(4)
                    .unitPrice(BigDecimal.TEN)
                    .subtotal(BigDecimal.valueOf(40))
                    .build();

            cart.setItems(new ArrayList<>(List.of(cartItem)));

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            // when + then
            InsufficientProductStockException exception = assertThrows(InsufficientProductStockException.class, () -> {
                cartService.addCartItem(addCartItemRequestDTO, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Product " + productName + " has Insufficient Stock!");

            then(cartRepository).should(never()).save(any(Cart.class));
        }


        @Test
        @DisplayName("Should Add Quantity When Product Already Exists In Cart")
        void shouldAddQuantityWhenProductAlreadyExistsInCart() {
            // given
            Product product = Product.builder()
                    .productId(productId)
                    .active(true)
                    .stockQuantity(2)
                    .price(BigDecimal.valueOf(50))
                    .build();

            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(50))
                    .subtotal(BigDecimal.valueOf(50))
                    .build();

            cart.setItems(new ArrayList<>(List.of(cartItem)));

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            Cart savedCart = Cart.builder()
                    .items(cart.getItems())
                    .build();

            given(cartRepository.save(cart)).willReturn(savedCart);

            // when
            AddedCartItemDTO result = cartService.addCartItem(addCartItemRequestDTO, userId);

            // then
            assertThat(result.quantity()).isEqualTo(2);
        }


        @Test
        @DisplayName("Should Add New Item When Product Not In Cart")
        void shouldAddNewItemWhenProductNotInCart() {
            // given
            Product product = Product.builder()
                    .productId(productId)
                    .active(Boolean.TRUE)
                    .stockQuantity(2)
                    .price(BigDecimal.valueOf(50))
                    .build();

            cart.setItems(new ArrayList<>());

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            Cart savedCart = Cart.builder()
                    .items(cart.getItems())
                    .build();

            given(cartRepository.save(cart)).willReturn(savedCart);

            // when
            AddedCartItemDTO result = cartService.addCartItem(addCartItemRequestDTO, userId);

            // then
            assertThat(result.quantity()).isEqualTo(1);
        }
    }


    @Nested
    @DisplayName("updateCartItemQuantity")
    class UpdateCartItemQuantity {
        UUID cartItemId;

        @BeforeEach
        void setUp() {
            cartItemId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Cart Item Not Found")
        void shouldThrowExceptionWhenCartItemNotFound() {
            // given
            UpdateCartItemQuantityRequestDTO requestDTO =
                    new UpdateCartItemQuantityRequestDTO(1);

            given(cartItemRepository.findById(cartItemId)).willReturn(Optional.empty());

            // when + then
            CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> {
                cartService.updateCartItemQuantity(requestDTO, cartItemId);
            });

            assertThat(exception.getMessage()).isEqualTo("Cart Item Not Found!");
        }


        @Test
        @DisplayName("Should Remove Cart Item If Quantity Equals Zero")
        void shouldRemoveCartItemIfQuantityEqualsZero() {
            // given
            UpdateCartItemQuantityRequestDTO requestDTO =
                    new UpdateCartItemQuantityRequestDTO(0);

            CartItem cartItem = CartItem.builder()
                    .cartItemId(cartItemId)
                    .build();

            given(cartItemRepository.findById(cartItemId)).willReturn(Optional.of(cartItem));
            given(cartItemRepository.existsById(cartItemId)).willReturn(true);

            // when
            cartService.updateCartItemQuantity(requestDTO, cartItemId);

            // then
            then(cartItemRepository).should().deleteById(cartItemId);
            then(cartRepository).should(never()).save(any());
        }


        @Test
        @DisplayName("Should Throw Exception When Insufficient Stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            // given
            String productName = "Test Product";

            Product product = Product.builder()
                    .name(productName)
                    .stockQuantity(5)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .cartItemId(cartItemId)
                    .product(product)
                    .quantity(2)
                    .build();

            UpdateCartItemQuantityRequestDTO requestDTO =
                    new UpdateCartItemQuantityRequestDTO(10);

            given(cartItemRepository.findById(cartItemId)).willReturn(Optional.of(cartItem));

            // when + then
            InsufficientProductStockException exception = assertThrows(InsufficientProductStockException.class, () -> {
                cartService.updateCartItemQuantity(requestDTO, cartItemId);
            });

            assertThat(exception.getMessage()).isEqualTo("Product " + productName + " has Insufficient Stock!");
        }


        @Test
        @DisplayName("Should Update Quantity Successfully")
        void shouldUpdateQuantitySuccessfully() {
            // given
            Product product = Product.builder()
                    .stockQuantity(100)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .cartItemId(cartItemId)
                    .product(product)
                    .quantity(5)
                    .unitPrice(BigDecimal.TEN)
                    .subtotal(BigDecimal.valueOf(50))
                    .build();

            UpdateCartItemQuantityRequestDTO requestDTO =
                    new UpdateCartItemQuantityRequestDTO(10);

            given(cartItemRepository.findById(cartItemId)).willReturn(Optional.of(cartItem));

            // when
            cartService.updateCartItemQuantity(requestDTO, cartItemId);

            // then
            assertThat(cartItem.getQuantity()).isEqualTo(10);
            assertThat(cartItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(100));
            then(cartItemRepository).should().save(cartItem);
        }
    }


    @Nested
    @DisplayName("removeCartItem")
    class RemoveCartItemTest {
        UUID itemId;

        @BeforeEach
        void setUp() {
            itemId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Cart Item Not Found")
        void shouldThrowExceptionWhenCartItemNotFound() {
            // given
            given(cartItemRepository.existsById(itemId)).willReturn(false);

            // when + then
            CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> {
                cartService.removeCartItem(itemId);
            });

            assertThat(exception.getMessage()).isEqualTo("Cart Item Not Found!");
        }

        @Test
        @DisplayName("Should Delete Cart Item When Exists")
        void shouldDeleteCartItemWhenExists() {
            // given
            given(cartItemRepository.existsById(itemId)).willReturn(true);

            // when
            cartService.removeCartItem(itemId);

            // then
            then(cartItemRepository).should().deleteById(itemId);
        }
    }


    @Nested
    @DisplayName("cleanCart")
    class CleanCart {
        UUID userId;
        UUID cartId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            cartId = UUID.randomUUID();
        }


        @Test
        @DisplayName("Should Throw Exception When Cart Is Empty")
        void shouldThrowExceptionWhenCartIsEmpty() {
            // given
            Cart cart = Cart.builder()
                    .items(new ArrayList<>())
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);

            // when + then
            CartIsEmptyException exception = assertThrows(CartIsEmptyException.class, () -> {
                cartService.cleanCart(userId);
            });

            assertThat(exception.getMessage()).isEqualTo("The Cart Is Already Empty!");
        }


        @Test
        @DisplayName("Should Clean Cart When Contains Items")
        void shouldCleanCartWhenContainsItems() {
            // given
            CartItem cartItem = CartItem.builder()
                    .quantity(1)
                    .build();

            Cart cart = Cart.builder()
                    .cartId(cartId)
                    .items(new ArrayList<>(List.of(cartItem)))
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);

            // when
            cartService.cleanCart(userId);

            // then
            then(cartItemRepository).should().deleteAllByCartId(cartId);
        }
    }
}
