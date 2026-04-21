package com.java.luismiguel.ecommerce_api.application.admin;

import com.java.luismiguel.ecommerce_api.api.dto.admin.request.ChangeUserRoleRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.admin.response.*;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminPanelService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminPanelService(UserRepository userRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Page<GetUsersResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new GetUsersResponseDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getUserRole(),
                        user.getActive(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                ));
    }


    public GetUserDetailsWithHistoryResponseDTO getUserDetails(UUID userId, Pageable pageable) {
         Page<Order> orders = orderRepository.findByUserId(userId, pageable);
         User user = orders.getContent().getFirst().getUser();

         return new GetUserDetailsWithHistoryResponseDTO(
                 user.getUserId(),
                 user.getUsername(),
                 user.getEmail(),
                 user.getUserRole(),
                 orders.map(order -> new OrdersHistoryResponseDTO(
                         order.getOrderId(),
                         order.getTotalAmount(),
                         order.getCreatedAt()
                 )),
                 user.getCreatedAt()
         );

    }


    public void changeUserRole(UUID userId, ChangeUserRoleRequestDTO changeUserRoleRequestDTO, User loggedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (userId.equals(loggedUser.getUserId())) {
            throw new UserCannotChangeOwnRoleException();
        }

        if (user.getUserRole().equals(changeUserRoleRequestDTO.role())) {
           if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) throw new UserRoleIsAlreadyAdminException();
           if (user.getUserRole().equals(UserRole.ROLE_CUSTOMER)) throw new UserRoleIsAlreadyCustomerException();
        }

        user.setUserRole(changeUserRoleRequestDTO.role());
        userRepository.save(user);
    }


    public void activeUserAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getActive()) {
            throw new UserAccountIsAlreadyActivatedException();
        }

        user.setActive(Boolean.TRUE);
        userRepository.save(user);
    }


    public void disableUserAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!user.getActive()) {
            throw new UserAccountIsAlreadyDeactivatedException();
        }

        user.setActive(Boolean.FALSE);
        userRepository.save(user);
    }


    public List<GetAllLowStockProductsDTO> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(product -> new GetAllLowStockProductsDTO(
                        product.getProductId(),
                        product.getName(),
                        product.getStockQuantity()
                )).toList();
    }


    public SalesReportDTO getTotalRevenuePerPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        BigDecimal totalRevenue = orderRepository.getTotalRevenueByPeriod(start, end);
        Long totalOrders = orderRepository.countOrdersByPeriod(start, end);

        return new SalesReportDTO(totalRevenue, totalOrders, startDate, endDate);
    }
}
