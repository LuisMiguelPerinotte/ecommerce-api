package com.java.luismiguel.ecommerce_api.application.payment;

import com.java.luismiguel.ecommerce_api.domain.address.Address;
import com.java.luismiguel.ecommerce_api.domain.address.AddressRepository;
import com.java.luismiguel.ecommerce_api.domain.category.Category;
import com.java.luismiguel.ecommerce_api.domain.category.CategoryRepository;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderItem;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.payment.Payment;
import com.java.luismiguel.ecommerce_api.domain.payment.PaymentRepository;
import com.java.luismiguel.ecommerce_api.domain.payment.enums.PaymentStatus;
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
public class PaymentRepositoryIntegrationTest {
    @Autowired
    private PaymentRepository paymentRepository;

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
    @DisplayName("Should Return True When Payment Exists By Order And Status")
    void shouldReturnTrueWhenPaymentExistsByOrderAndStatus() {
        // given
        Order order = saveOrder("Miguel", "miguel@email.com", OrderStatus.AWAITING_PAYMENT);

        Payment payment = Payment.builder()
                .order(order)
                .stripeSessionId("cs_test_123")
                .paymentIntentId("pi_test_123")
                .amount(BigDecimal.valueOf(100))
                .currency("brl")
                .status(PaymentStatus.CREATED)
                .build();

        paymentRepository.save(payment);

        // when
        Boolean result = paymentRepository.existsByOrderAndStatus(order, PaymentStatus.CREATED);

        // then
        assertThat(result).isTrue();
    }


    @Test
    @DisplayName("Should Return False When Payment Does Not Exist By Order And Status")
    void shouldReturnFalseWhenPaymentDoesNotExistByOrderAndStatus() {
        // given
        Order order = saveOrder("Miguel", "miguel@email.com", OrderStatus.AWAITING_PAYMENT);

        Payment payment = Payment.builder()
                .order(order)
                .stripeSessionId("cs_test_123")
                .paymentIntentId("pi_test_123")
                .amount(BigDecimal.valueOf(100))
                .currency("brl")
                .status(PaymentStatus.APPROVED)
                .build();

        paymentRepository.save(payment);

        // when
        Boolean result = paymentRepository.existsByOrderAndStatus(order, PaymentStatus.CREATED);

        // then
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Should Find Payment By Stripe Session Id With Order")
    void shouldFindPaymentByStripeSessionIdWithOrder() {
        // given
        Order order = saveOrder("Miguel", "miguel@email.com", OrderStatus.AWAITING_PAYMENT);

        Payment payment = Payment.builder()
                .order(order)
                .stripeSessionId("cs_test_123")
                .paymentIntentId("pi_test_123")
                .amount(BigDecimal.valueOf(100))
                .currency("brl")
                .status(PaymentStatus.CREATED)
                .build();

        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        // when
        Optional<Payment> result = paymentRepository.findByStripeSessionIdWithOrder("cs_test_123");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentId()).isEqualTo(savedPayment.getPaymentId());
        assertThat(result.get().getStripeSessionId()).isEqualTo("cs_test_123");
        assertThat(result.get().getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(result.get().getOrder()).isNotNull();
        assertThat(result.get().getOrder().getOrderId()).isEqualTo(order.getOrderId());
    }


    @Test
    @DisplayName("Should Return Empty When Stripe Session Id Does Not Exist")
    void shouldReturnEmptyWhenStripeSessionIdDoesNotExist() {
        // given
        Order order = saveOrder("Miguel", "miguel@email.com", OrderStatus.AWAITING_PAYMENT);

        Payment payment = Payment.builder()
                .order(order)
                .stripeSessionId("cs_test_123")
                .paymentIntentId("pi_test_123")
                .amount(BigDecimal.valueOf(100))
                .currency("brl")
                .status(PaymentStatus.CREATED)
                .build();

        paymentRepository.save(payment);

        // when
        Optional<Payment> result = paymentRepository.findByStripeSessionIdWithOrder("cs_not_found");

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("Should Find Payment By Order Id And Status With Order")
    void shouldFindPaymentByOrderIdAndStatusWithOrder() {
        // given
        Order order = saveOrder("Miguel", "miguel@email.com", OrderStatus.AWAITING_PAYMENT);

        Payment payment = Payment.builder()
                .order(order)
                .stripeSessionId("cs_test_123")
                .paymentIntentId("pi_test_123")
                .amount(BigDecimal.valueOf(100))
                .currency("brl")
                .status(PaymentStatus.CREATED)
                .build();

        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        // when
        Optional<Payment> result = paymentRepository.findPaymentByOrderIdAndStatusWithOrder(
                order.getOrderId(),
                PaymentStatus.CREATED
        );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentId()).isEqualTo(savedPayment.getPaymentId());
        assertThat(result.get().getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(result.get().getOrder()).isNotNull();
        assertThat(result.get().getOrder().getOrderId()).isEqualTo(order.getOrderId());
    }


    @Test
    @DisplayName("Should Return Empty When Payment By Order Id And Status Does Not Exist")
    void shouldReturnEmptyWhenPaymentByOrderIdAndStatusDoesNotExist() {
        // given
        Order order = saveOrder("Miguel", "miguel@email.com", OrderStatus.AWAITING_PAYMENT);

        Payment payment = Payment.builder()
                .order(order)
                .stripeSessionId("cs_test_123")
                .paymentIntentId("pi_test_123")
                .amount(BigDecimal.valueOf(100))
                .currency("brl")
                .status(PaymentStatus.APPROVED)
                .build();

        paymentRepository.save(payment);

        // when
        Optional<Payment> result = paymentRepository.findPaymentByOrderIdAndStatusWithOrder(
                order.getOrderId(),
                PaymentStatus.CREATED
        );

        // then
        assertThat(result).isEmpty();
    }


    private Order saveOrder(String username, String email, OrderStatus orderStatus) {
        User user = saveUser(username, email);
        Address address = saveAddress(user);
        Product product = saveProduct("Mouse", BigDecimal.valueOf(100));

        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .orderStatus(orderStatus)
                .totalAmount(BigDecimal.valueOf(100))
                .userNotes("user notes")
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .productName(product.getName())
                .productSku(product.getSku())
                .unitPrice(BigDecimal.valueOf(100))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(100))
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