package com.java.luismiguel.ecommerce_api.application.order;

import com.java.luismiguel.ecommerce_api.domain.address.Address;
import com.java.luismiguel.ecommerce_api.domain.address.AddressRepository;
import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderItem;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.OrderStatusCountProjection;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class OrderRepositoryIntegrationTest {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should Find Order By Id With Items")
    void shouldFindOrderByIdWithItems() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(200))
                .userNotes("user notes")
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .productName(product.getName())
                .productSku(product.getSku())
                .unitPrice(BigDecimal.valueOf(100))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(200))
                .build();

        savedOrder.setItems(new ArrayList<>(List.of(orderItem)));
        orderRepository.saveAndFlush(savedOrder);

        // when
        Optional<Order> result = orderRepository.findByIdWithItems(savedOrder.getOrderId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(savedOrder.getOrderId());
        assertThat(result.get().getItems()).hasSize(1);
        assertThat(result.get().getItems().getFirst().getProductName()).isEqualTo("Mouse");
        assertThat(result.get().getItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(result.get().getItems().getFirst().getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }


    @Test
    @DisplayName("Should Find Orders By User User Id")
    void shouldFindOrdersByUserUserId() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        User anotherUser = saveUser("João", "joao@email.com");

        Address address = saveAddress(user);
        Address anotherAddress = saveAddress(anotherUser);

        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        Order userOrder = saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(anotherUser, anotherAddress, product, OrderStatus.PAID, BigDecimal.valueOf(200));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Order> result = orderRepository.findByUserUserId(user.getUserId(), pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getOrderId()).isEqualTo(userOrder.getOrderId());
        assertThat(result.getContent().getFirst().getUser().getUserId()).isEqualTo(user.getUserId());
        assertThat(result.getContent().getFirst().getItems()).hasSize(1);
    }


    @Test
    @DisplayName("Should Find Orders By User Id")
    void shouldFindOrdersByUserId() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        User anotherUser = saveUser("João", "joao@email.com");

        Address address = saveAddress(user);
        Address anotherAddress = saveAddress(anotherUser);

        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        Order userOrder = saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(anotherUser, anotherAddress, product, OrderStatus.PAID, BigDecimal.valueOf(200));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Order> result = orderRepository.findByUserId(user.getUserId(), pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getOrderId()).isEqualTo(userOrder.getOrderId());
        assertThat(result.getContent().getFirst().getUser().getUserId()).isEqualTo(user.getUserId());
    }


    @Test
    @DisplayName("Should Get Total Revenue")
    void shouldGetTotalRevenue() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(user, address, product, OrderStatus.SHIPPED, BigDecimal.valueOf(200));
        saveOrderWithItem(user, address, product, OrderStatus.DELIVERED, BigDecimal.valueOf(300));
        saveOrderWithItem(user, address, product, OrderStatus.PENDING, BigDecimal.valueOf(400));
        saveOrderWithItem(user, address, product, OrderStatus.CANCELLED, BigDecimal.valueOf(500));

        // when
        BigDecimal result = orderRepository.getTotalRevenue();

        // then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(600));
    }


    @Test
    @DisplayName("Should Count Total Orders")
    void shouldCountTotalOrders() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(user, address, product, OrderStatus.PROCESSING, BigDecimal.valueOf(200));
        saveOrderWithItem(user, address, product, OrderStatus.SHIPPED, BigDecimal.valueOf(300));
        saveOrderWithItem(user, address, product, OrderStatus.DELIVERED, BigDecimal.valueOf(400));
        saveOrderWithItem(user, address, product, OrderStatus.PENDING, BigDecimal.valueOf(500));
        saveOrderWithItem(user, address, product, OrderStatus.CANCELLED, BigDecimal.valueOf(600));

        // when
        Long result = orderRepository.countTotalOrders();

        // then
        assertThat(result).isEqualTo(4L);
    }


    @Test
    @DisplayName("Should Get Order Count By Status")
    void shouldGetOrderCountByStatus() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(200));
        saveOrderWithItem(user, address, product, OrderStatus.PENDING, BigDecimal.valueOf(300));

        // when
        List<OrderStatusCountProjection> result = orderRepository.getOrderCountByStatus();

        // then
        assertThat(result).hasSize(2);

        Long paidCount = result.stream()
                .filter(item -> item.getStatus() == OrderStatus.PAID)
                .findFirst()
                .orElseThrow()
                .getCount();

        Long pendingCount = result.stream()
                .filter(item -> item.getStatus() == OrderStatus.PENDING)
                .findFirst()
                .orElseThrow()
                .getCount();

        assertThat(paidCount).isEqualTo(2L);
        assertThat(pendingCount).isEqualTo(1L);
    }


    @Test
    @DisplayName("Should Get Revenue Today")
    void shouldGetRevenueToday() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(user, address, product, OrderStatus.PENDING, BigDecimal.valueOf(200));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // when
        BigDecimal result = orderRepository.getRevenueToday(startOfDay, endOfDay);

        // then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
    }


    @Test
    @DisplayName("Should Count Orders Today")
    void shouldCountOrdersToday() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(user, address, product, OrderStatus.PROCESSING, BigDecimal.valueOf(200));
        saveOrderWithItem(user, address, product, OrderStatus.PENDING, BigDecimal.valueOf(300));
        saveOrderWithItem(user, address, product, OrderStatus.CANCELLED, BigDecimal.valueOf(400));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // when
        Long result = orderRepository.countOrdersToday(startOfDay, endOfDay);

        // then
        assertThat(result).isEqualTo(2L);
    }


    @Test
    @DisplayName("Should Get Total Revenue By Period")
    void shouldGetTotalRevenueByPeriod() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(user, address, product, OrderStatus.SHIPPED, BigDecimal.valueOf(200));
        saveOrderWithItem(user, address, product, OrderStatus.DELIVERED, BigDecimal.valueOf(300));
        saveOrderWithItem(user, address, product, OrderStatus.PROCESSING, BigDecimal.valueOf(400));
        saveOrderWithItem(user, address, product, OrderStatus.PENDING, BigDecimal.valueOf(500));

        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        // when
        BigDecimal result = orderRepository.getTotalRevenueByPeriod(start, end);

        // then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(600));
    }


    @Test
    @DisplayName("Should Count Orders By Period")
    void shouldCountOrdersByPeriod() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        saveOrderWithItem(user, address, product, OrderStatus.PAID, BigDecimal.valueOf(100));
        saveOrderWithItem(user, address, product, OrderStatus.PROCESSING, BigDecimal.valueOf(200));
        saveOrderWithItem(user, address, product, OrderStatus.SHIPPED, BigDecimal.valueOf(300));
        saveOrderWithItem(user, address, product, OrderStatus.DELIVERED, BigDecimal.valueOf(400));
        saveOrderWithItem(user, address, product, OrderStatus.PENDING, BigDecimal.valueOf(500));

        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        // when
        Long result = orderRepository.countOrdersByPeriod(start, end);

        // then
        assertThat(result).isEqualTo(4L);
    }


    private Order saveOrderWithItem(
            User user,
            Address address,
            Product product,
            OrderStatus status,
            BigDecimal totalAmount
    ) {
        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .orderStatus(status)
                .totalAmount(totalAmount)
                .userNotes("user notes")
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .productName(product.getName())
                .productSku(product.getSku())
                .unitPrice(totalAmount)
                .quantity(1)
                .subtotal(totalAmount)
                .build();

        savedOrder.setItems(new ArrayList<>(List.of(orderItem)));

        return orderRepository.saveAndFlush(savedOrder);
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


    private Address saveAddress(User user) {
        Address address = Address.builder()
                .user(user)
                .street("Rua " + user.getUsername())
                .houseNumber("123")
                .complement("Casa")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01001-000")
                .isDefault(true)
                .active(true)
                .build();

        return addressRepository.save(address);
    }


    private Category saveCategory(String name) {
        Category category = Category.builder()
                .name("Categoria " + name)
                .description("Categoria de " + name)
                .slug("categoria-" + name.toLowerCase())
                .active(true)
                .build();

        return categoryRepository.save(category);
    }


    private Product saveProduct(String name, BigDecimal price) {
        Category category = saveCategory(name);

        Product product = Product.builder()
                .name(name)
                .description(name + " description")
                .price(price)
                .stockQuantity(10)
                .imageUrl("https://image.com/" + name)
                .sku("SKU-" + name)
                .active(true)
                .category(category)
                .build();

        return productRepository.save(product);
    }
}