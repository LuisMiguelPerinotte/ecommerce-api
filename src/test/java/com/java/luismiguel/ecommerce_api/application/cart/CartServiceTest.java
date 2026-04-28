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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

            GetCartResponseDTO result = cartService.getCart(userId);

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

            GetCartResponseDTO result = cartService.getCart(userId);

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
            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            Assertions.assertThrows(ProductNotFoundException.class, () -> {
                cartService.addCartItem(addCartItemRequestDTO, userId);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Product Is Not Active")
        void shouldThrowExceptionWhenProductIsNotActive() {
            Product product = Product.builder()
                    .productId(productId)
                    .active(false)
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            Assertions.assertThrows(ProductNotFoundException.class, () -> {
                cartService.addCartItem(addCartItemRequestDTO, userId);
            });
        }


        @Test
        @DisplayName("Should Throw Exception When Product Has Insufficient Stock")
        void shouldThrowExceptionWhenProductHasInsufficientStock() {
            Product product = Product.builder()
                    .active(Boolean.TRUE)
                    .stockQuantity(0)
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            Assertions.assertThrows(InsufficientProductStockException.class, () -> {
                cartService.addCartItem(addCartItemRequestDTO, userId);
            });
        }


        @Test
        @DisplayName("Should Add Quantity When Product Already Exists In Cart")
        void shouldAddQuantityWhenProductAlreadyExistsInCart() {
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

            cart.setItems(List.of(cartItem));

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            Cart savedCart = Cart.builder()
                    .items(cart.getItems())
                    .build();

            given(cartRepository.save(cart)).willReturn(savedCart);

            AddedCartItemDTO result = cartService.addCartItem(addCartItemRequestDTO, userId);

            Assertions.assertEquals(2, result.quantity());
        }


        @Test
        @DisplayName("Should Add New Item When Product Not In Cart")
        void shouldAddNewItemWhenProductNotInCart() {
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

            AddedCartItemDTO result = cartService.addCartItem(addCartItemRequestDTO, userId);

            Assertions.assertEquals(1, result.quantity());
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
            UpdateCartItemQuantityRequestDTO requestDTO =
                    new UpdateCartItemQuantityRequestDTO(1);

            given(cartItemRepository.findById(cartItemId)).willReturn(Optional.empty());

            Assertions.assertThrows(CartItemNotFoundException.class, () -> {
                cartService.updateCartItemQuantity(requestDTO, cartItemId);
            });
        }


        @Test
        @DisplayName("Should Remove Cart Item If Quantity Equals Zero")
        void shouldRemoveCartItemIfQuantityEqualsZero() {
            UpdateCartItemQuantityRequestDTO requestDTO =
                    new UpdateCartItemQuantityRequestDTO(0);

            CartItem cartItem = CartItem.builder()
                    .cartItemId(cartItemId)
                    .build();

            given(cartItemRepository.findById(cartItemId)).willReturn(Optional.of(cartItem));
            given(cartItemRepository.existsById(cartItemId)).willReturn(true);

            cartService.updateCartItemQuantity(requestDTO, cartItemId);

            then(cartItemRepository).should().deleteById(cartItemId);
        }


        @Test
        @DisplayName("Should Throw Exception When Insufficient Stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            Product product = Product.builder()
                    .name("Test Product")
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

            Assertions.assertThrows(InsufficientProductStockException.class, () -> {
                cartService.updateCartItemQuantity(requestDTO, cartItemId);
            });
        }


        @Test
        @DisplayName("Should Update Quantity Successfully")
        void shouldUpdateQuantitySuccessfully() {
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

            cartService.updateCartItemQuantity(requestDTO, cartItemId);

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
            given(cartItemRepository.existsById(itemId)).willReturn(false);

            Assertions.assertThrows(CartItemNotFoundException.class, () -> {
                cartService.removeCartItem(itemId);
            });
        }

        @Test
        @DisplayName("Should Delete Cart Item When Exists")
        void shouldDeleteCartItemWhenExists() {
            given(cartItemRepository.existsById(itemId)).willReturn(true);

            cartService.removeCartItem(itemId);
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
            Cart cart = Cart.builder()
                    .items(new ArrayList<>())
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);

            Assertions.assertThrows(CartIsEmptyException.class, () -> {
                cartService.cleanCart(userId);
            });
        }


        @Test
        @DisplayName("Should Clean Cart When Contains Items")
        void shouldCleanCartWhenContainsItems() {
            CartItem cartItem = CartItem.builder()
                    .quantity(1)
                    .build();

            Cart cart = Cart.builder()
                    .cartId(cartId)
                    .items(List.of(cartItem))
                    .build();

            given(cartRepository.findByUserUserId(userId)).willReturn(cart);
            cartService.cleanCart(userId);
            then(cartItemRepository).should().deleteAllByCartId(cartId);
        }
    }
}
