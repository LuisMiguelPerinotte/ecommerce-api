package com.java.luismiguel.ecommerce_api.application.admin;

import com.java.luismiguel.ecommerce_api.api.dto.admin.response.DashboardResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.OrderStatusCountProjection;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public AdminDashboardService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    @Cacheable(value = "adminDashboard", unless = "#result == null")
    public DashboardResponseDTO getDashboard() {
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        Long totalOrders = orderRepository.countTotalOrders();

        Map<OrderStatus, Long> ordersByStatus = orderRepository.getOrderCountByStatus()
                .stream()
                .collect(Collectors.toMap(
                        OrderStatusCountProjection::getStatus,
                        OrderStatusCountProjection::getCount
                ));

        Long totalProducts = productRepository.count();
        Integer lowStockProducts = productRepository.countLowStockProducts(10);
        Long totalUsers = userRepository.countActiveCustomers();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal revenueToday = orderRepository.getRevenueToday(startOfDay, endOfDay);
        Long ordersToday = orderRepository.countOrdersToday(startOfDay, endOfDay);

        return new DashboardResponseDTO(
                totalRevenue,
                totalOrders,
                ordersByStatus,
                totalProducts,
                lowStockProducts,
                totalUsers,
                revenueToday,
                ordersToday,
                LocalDateTime.now()
        );
    }
}
