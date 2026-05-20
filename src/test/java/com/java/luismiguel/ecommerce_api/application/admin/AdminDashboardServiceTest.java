package com.java.luismiguel.ecommerce_api.application.admin;

import com.java.luismiguel.ecommerce_api.api.dto.admin.response.DashboardResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.OrderStatusCountProjection;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    @DisplayName("Should Return Dashboard Successfully")
    void shouldReturnDashboardSuccessfully() {
        // given
        BigDecimal totalRevenue = BigDecimal.valueOf(1000);
        Long totalOrders = 20L;
        Long totalProducts = 50L;
        Integer lowStockProducts = 5;
        Long totalUsers = 10L;
        BigDecimal revenueToday = BigDecimal.valueOf(250);
        Long ordersToday = 4L;

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        OrderStatusCountProjection paidProjection = mock(OrderStatusCountProjection.class);
        OrderStatusCountProjection pendingProjection = mock(OrderStatusCountProjection.class);

        given(paidProjection.getStatus()).willReturn(OrderStatus.PAID);
        given(paidProjection.getCount()).willReturn(12L);

        given(pendingProjection.getStatus()).willReturn(OrderStatus.PENDING);
        given(pendingProjection.getCount()).willReturn(8L);

        given(orderRepository.getTotalRevenue()).willReturn(totalRevenue);
        given(orderRepository.countTotalOrders()).willReturn(totalOrders);
        given(orderRepository.getOrderCountByStatus()).willReturn(List.of(paidProjection, pendingProjection));
        given(productRepository.count()).willReturn(totalProducts);
        given(productRepository.countLowStockProducts(10)).willReturn(lowStockProducts);
        given(userRepository.countActiveCustomers()).willReturn(totalUsers);
        given(orderRepository.getRevenueToday(startOfDay, endOfDay)).willReturn(revenueToday);
        given(orderRepository.countOrdersToday(startOfDay, endOfDay)).willReturn(ordersToday);

        // when
        DashboardResponseDTO result = adminDashboardService.getDashboard();

        // then
        assertThat(result.totalRevenue()).isEqualByComparingTo(totalRevenue);
        assertThat(result.totalOrders()).isEqualTo(totalOrders);
        assertThat(result.totalProducts()).isEqualTo(totalProducts);
        assertThat(result.lowStockProducts()).isEqualTo(lowStockProducts);
        assertThat(result.totalUsers()).isEqualTo(totalUsers);
        assertThat(result.revenueToday()).isEqualByComparingTo(revenueToday);
        assertThat(result.ordersToday()).isEqualTo(ordersToday);
        assertThat(result.generatedAt()).isNotNull();

        assertThat(result.ordersByStatus()).hasSize(2);
        assertThat(result.ordersByStatus()).containsEntry(OrderStatus.PAID, 12L);
        assertThat(result.ordersByStatus()).containsEntry(OrderStatus.PENDING, 8L);

        then(orderRepository).should().getTotalRevenue();
        then(orderRepository).should().countTotalOrders();
        then(orderRepository).should().getOrderCountByStatus();
        then(productRepository).should().count();
        then(productRepository).should().countLowStockProducts(10);
        then(userRepository).should().countActiveCustomers();
        then(orderRepository).should().getRevenueToday(startOfDay, endOfDay);
        then(orderRepository).should().countOrdersToday(startOfDay, endOfDay);
    }
}