package com.java.luismiguel.ecommerce_api.application.order;

import com.java.luismiguel.ecommerce_api.api.dto.order.request.CreateOrderRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.order.response.CreatedOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.order.response.GetAllUserOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.order.response.GetOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.order.response.ListOrderItemResponseDTO;
import com.java.luismiguel.ecommerce_api.application.cart.CartService;
import com.java.luismiguel.ecommerce_api.domain.address.Address;
import com.java.luismiguel.ecommerce_api.domain.address.AddressRepository;
import com.java.luismiguel.ecommerce_api.domain.cart.Cart;
import com.java.luismiguel.ecommerce_api.domain.cart.CartItem;
import com.java.luismiguel.ecommerce_api.domain.cart.CartRepository;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderItem;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.NoDefaultAddressRegisteredException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart.CartIsEmptyException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart.CartNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order.OrderNotCancellableException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order.OrderNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.InsufficientProductStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {
        UUID userId;
        UUID addressId;
        UUID cartId;
        UUID productId;
        UUID orderId;
        UUID orderItemId;
        CreateOrderRequestDTO requestDTO;
        User user;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            addressId = UUID.randomUUID();
            cartId = UUID.randomUUID();
            productId = UUID.randomUUID();
            orderId = UUID.randomUUID();
            orderItemId = UUID.randomUUID();

            requestDTO = new CreateOrderRequestDTO(addressId, "userNotes");

            user = User.builder()
                    .userId(userId)
                    .build();
        }

        @Test
        @DisplayName("Should Create Order Successfully")
        void shouldCreateOrderSuccessfully() {
            // given
            Product product = Product.builder()
                    .productId(productId)
                    .name("Product")
                    .stockQuantity(10)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .unitPrice(BigDecimal.TEN)
                    .quantity(2)
                    .subtotal(BigDecimal.valueOf(20))
                    .build();

            Cart cart = Cart.builder()
                    .cartId(cartId)
                    .items(List.of(cartItem))
                    .build();

            Address address = Address.builder()
                    .addressId(addressId)
                    .user(user)
                    .build();

            given(cartRepository.findByUserUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));

            given(addressRepository.findById(addressId))
                    .willReturn(Optional.of(address));

            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> {
                        Order order = invocation.getArgument(0);

                        order.setOrderId(orderId);
                        order.setCreatedAt(LocalDateTime.now());

                        order.getItems().getFirst().setOrderItemId(orderItemId);

                        return order;
                    });



            // when
            CreatedOrderResponseDTO result = orderService.createOrder(requestDTO, user);

            // then
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.orderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
            assertThat(result.items()).hasSize(1);
            assertThat(result.createdAt()).isNotNull();

            ListOrderItemResponseDTO itemDTO = result.items().getFirst();

            assertThat(itemDTO.orderItemId()).isEqualTo(orderItemId);
            assertThat(itemDTO.productId()).isEqualTo(productId);
            assertThat(itemDTO.productName()).isEqualTo("Product");
            assertThat(itemDTO.unitPrice()).isEqualByComparingTo(BigDecimal.TEN);
            assertThat(itemDTO.quantity()).isEqualTo(2);
            assertThat(itemDTO.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(20));

            assertThat(product.getStockQuantity()).isEqualTo(8);

            then(orderRepository).should().save(any(Order.class));
            then(productRepository).should().save(product);
            then(cartService).should().cleanCart(userId);
        }


        @Test
        @DisplayName("Should Throw Exception When Cart Not Found")
        void shouldThrowExceptionWhenCartNotFound() {
            // given
            given(cartRepository.findByUserUserIdWithItems(user.getUserId())).willReturn(Optional.empty());

            // when + then
            CartNotFoundException exception = assertThrows(CartNotFoundException.class, () -> {
                orderService.createOrder(requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Cart Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Cart Is Empty")
        void shouldThrowExceptionWhenCartIsEmpty() {
            // given
            Cart cart = Cart.builder()
                    .items(List.of())
                    .build();

            given(cartRepository.findByUserUserIdWithItems(user.getUserId())).willReturn(Optional.of(cart));

            // when + then
            CartIsEmptyException exception = assertThrows(CartIsEmptyException.class, () -> {
                orderService.createOrder(requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("The Cart Is Already Empty!");
        }


        @Test
        @DisplayName("Should Throw Exception When Product Has Insufficient Stock")
        void shouldThrowExceptionWhenProductHasInsufficientStock() {
            // given
            String productName = "productName";

            User user = User.builder()
                    .userId(userId)
                    .build();

            Product product = Product.builder()
                    .name(productName)
                    .stockQuantity(0)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .quantity(1)
                    .build();

            Cart cart = Cart.builder()
                    .items(List.of(cartItem))
                    .build();

            given(cartRepository.findByUserUserIdWithItems(user.getUserId())).willReturn(Optional.of(cart));

            // when + then
            InsufficientProductStockException exception = assertThrows(InsufficientProductStockException.class, () -> {
                orderService.createOrder(requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Product " + productName + " has Insufficient Stock!");
        }


        @Test
        @DisplayName("Should Throw Exception When User Does Not Have Default Address")
        void shouldThrowExceptionWhenUserDoesNotHaveDefaultAddress() {
            //given
            CreateOrderRequestDTO requestDTOAddressNull = new CreateOrderRequestDTO(null, "UserNotes");

            Product product = Product.builder()
                    .stockQuantity(1)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .quantity(1)
                    .build();

            Cart cart = Cart.builder()
                    .items(List.of(cartItem))
                    .build();

            given(cartRepository.findByUserUserIdWithItems(user.getUserId())).willReturn(Optional.of(cart));
            given(addressRepository.findByUserUserIdAndIsDefaultTrue(user.getUserId())).willReturn(Optional.empty());

            // when + then
            NoDefaultAddressRegisteredException exception = assertThrows(NoDefaultAddressRegisteredException.class, () -> {
                orderService.createOrder(requestDTOAddressNull, user);
            });

            assertThat(exception.getMessage()).isEqualTo("No Default Address Registered");
        }


        @Test
        @DisplayName("Should Throw Exception When Provided Address Not Found")
        void shouldThrowExceptionWhenProvidedAddressNotFound() {
            Product product = Product.builder()
                    .stockQuantity(1)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .quantity(1)
                    .build();

            Cart cart = Cart.builder()
                    .items(List.of(cartItem))
                    .build();

            given(cartRepository.findByUserUserIdWithItems(user.getUserId())).willReturn(Optional.of(cart));
            given(addressRepository.findById(requestDTO.addressId())).willReturn(Optional.empty());

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                orderService.createOrder(requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Belongs To Another User")
        void shouldThrowExceptionWhenAddressBelongsToAnotherUser() {
            Product product = Product.builder()
                    .stockQuantity(1)
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .quantity(1)
                    .build();

            Cart cart = Cart.builder()
                    .items(List.of(cartItem))
                    .build();

            Address address = Address.builder()
                    .user(User.builder()
                            .userId(UUID.randomUUID())
                            .build())
                    .build();

            given(cartRepository.findByUserUserIdWithItems(user.getUserId())).willReturn(Optional.of(cart));
            given(addressRepository.findById(requestDTO.addressId())).willReturn(Optional.of(address));

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                orderService.createOrder(requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }
    }


    @Nested
    @DisplayName("getAllUserOrders")
    class GetAllUserOrders {
        UUID userId;
        UUID orderId;
        UUID orderItemId;
        UUID productId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            orderId = UUID.randomUUID();
            orderItemId = UUID.randomUUID();
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return User Orders")
        void shouldReturnUserOrders() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("Product")
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .orderItemId(orderItemId)
                    .product(product)
                    .productName("Product")
                    .unitPrice(BigDecimal.TEN)
                    .quantity(2)
                    .subtotal(BigDecimal.valueOf(20))
                    .build();

            LocalDateTime createdAt = LocalDateTime.now();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .items(List.of(orderItem))
                    .orderStatus(OrderStatus.PENDING)
                    .totalAmount(BigDecimal.valueOf(20))
                    .createdAt(createdAt)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orders = new PageImpl<>(List.of(order));

            given(orderRepository.findByUserUserId(userId, pageable)).willReturn(orders);

            // when
            Page<GetAllUserOrderResponseDTO> result = orderService.getAllUserOrders(pageable, user);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);

            GetAllUserOrderResponseDTO dto = result.getContent().getFirst();

            assertThat(dto.orderId()).isEqualTo(orderId);
            assertThat(dto.orderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(dto.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
            assertThat(dto.createdAt()).isEqualTo(createdAt);
            assertThat(dto.items()).hasSize(1);

            ListOrderItemResponseDTO itemDTO = dto.items().getFirst();

            assertThat(itemDTO.orderItemId()).isEqualTo(orderItemId);
            assertThat(itemDTO.productId()).isEqualTo(productId);
            assertThat(itemDTO.productName()).isEqualTo("Product");
            assertThat(itemDTO.unitPrice()).isEqualByComparingTo(BigDecimal.TEN);
            assertThat(itemDTO.quantity()).isEqualTo(2);
            assertThat(itemDTO.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(20));

            then(orderRepository).should().findByUserUserId(userId, pageable);
        }
    }


    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {
        UUID orderId;
        UUID orderItemId;
        UUID productId;
        UUID addressId;

        @BeforeEach
        void setUp() {
            orderId = UUID.randomUUID();
            orderItemId = UUID.randomUUID();
            productId = UUID.randomUUID();
            addressId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Order Not Found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // given
            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // when + then
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
                orderService.getOrderById(orderId);
            });

            assertThat(exception.getMessage()).isEqualTo("Order Not Found!");
        }


        @Test
        @DisplayName("Should Return Order By Id Successfully")
        void shouldReturnOrderByIdSuccessfully() {
            // given
            Product product = Product.builder()
                    .productId(productId)
                    .name("Product")
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .orderItemId(orderItemId)
                    .product(product)
                    .productName("Product")
                    .unitPrice(BigDecimal.TEN)
                    .quantity(2)
                    .subtotal(BigDecimal.valueOf(20))
                    .build();

            Address address = Address.builder()
                    .addressId(addressId)
                    .street("street")
                    .houseNumber("123")
                    .complement("complement")
                    .neighborhood("neighborhood")
                    .city("city")
                    .state("state")
                    .zipCode("01001-000")
                    .isDefault(false)
                    .build();

            LocalDateTime createdAt = LocalDateTime.now();

            Order order = Order.builder()
                    .orderId(orderId)
                    .items(List.of(orderItem))
                    .totalAmount(BigDecimal.valueOf(20))
                    .orderStatus(OrderStatus.PENDING)
                    .shippingAddress(address)
                    .userNotes("user notes")
                    .createdAt(createdAt)
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when
            GetOrderResponseDTO result = orderService.getOrderById(orderId);

            // then
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
            assertThat(result.orderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.userNotes()).isEqualTo("user notes");
            assertThat(result.createdAt()).isEqualTo(createdAt);

            assertThat(result.items()).hasSize(1);

            ListOrderItemResponseDTO itemDTO = result.items().getFirst();

            assertThat(itemDTO.orderItemId()).isEqualTo(orderItemId);
            assertThat(itemDTO.productId()).isEqualTo(productId);
            assertThat(itemDTO.productName()).isEqualTo("Product");
            assertThat(itemDTO.unitPrice()).isEqualByComparingTo(BigDecimal.TEN);
            assertThat(itemDTO.quantity()).isEqualTo(2);
            assertThat(itemDTO.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(20));

            assertThat(result.shippingAddress().addressId()).isEqualTo(addressId);
            assertThat(result.shippingAddress().street()).isEqualTo("street");
            assertThat(result.shippingAddress().number()).isEqualTo("123");
            assertThat(result.shippingAddress().complement()).isEqualTo("complement");
            assertThat(result.shippingAddress().neighborhood()).isEqualTo("neighborhood");
            assertThat(result.shippingAddress().city()).isEqualTo("city");
            assertThat(result.shippingAddress().state()).isEqualTo("state");
            assertThat(result.shippingAddress().zipCode()).isEqualTo("01001-000");
            assertThat(result.shippingAddress().isDefault()).isFalse();
        }
    }


    @Nested
    @DisplayName("cancelOrderById")
    class CancelOrderById {
        UUID userId;
        UUID orderId;
        UUID productId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            orderId = UUID.randomUUID();
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Order Not Found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // when + then
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
                orderService.cancelOrderById(orderId, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Order Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Order Belongs To Another User")
        void shouldThrowExceptionWhenOrderBelongsToAnotherUser() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            User anotherUser = User.builder()
                    .userId(UUID.randomUUID())
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(anotherUser)
                    .orderStatus(OrderStatus.PENDING)
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when + then
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
                orderService.cancelOrderById(orderId, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Order Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Order Is Not Cancellable")
        void shouldThrowExceptionWhenOrderIsNotCancellable() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when + then
            OrderNotCancellableException exception = assertThrows(OrderNotCancellableException.class, () -> {
                orderService.cancelOrderById(orderId, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Order Cannot Be Cancelled In Its Current Status!");
        }


        @Test
        @DisplayName("Should Cancel Pending Order Successfully")
        void shouldCancelPendingOrderSuccessfully() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("Product")
                    .stockQuantity(5)
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(2)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .orderStatus(OrderStatus.PENDING)
                    .items(List.of(orderItem))
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when
            orderService.cancelOrderById(orderId, user);

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(product.getStockQuantity()).isEqualTo(7);

            then(productRepository).should().save(product);
            then(orderRepository).should().save(order);
        }


        @Test
        @DisplayName("Should Cancel Awaiting Payment Order Successfully")
        void shouldCancelAwaitingPaymentOrderSuccessfully() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Product product = Product.builder()
                    .productId(productId)
                    .name("Product")
                    .stockQuantity(10)
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(3)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .orderStatus(OrderStatus.AWAITING_PAYMENT)
                    .items(List.of(orderItem))
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when
            orderService.cancelOrderById(orderId, user);

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(product.getStockQuantity()).isEqualTo(13);

            then(productRepository).should().save(product);
            then(orderRepository).should().save(order);
        }
    }
}
