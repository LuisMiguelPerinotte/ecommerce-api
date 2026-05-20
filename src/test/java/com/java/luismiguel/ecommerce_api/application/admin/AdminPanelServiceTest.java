package com.java.luismiguel.ecommerce_api.application.admin;

import com.java.luismiguel.ecommerce_api.api.dto.admin.request.ChangeUserRoleRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.admin.response.GetAllLowStockProductsDTO;
import com.java.luismiguel.ecommerce_api.api.dto.admin.response.GetUserDetailsWithHistoryResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.admin.response.GetUsersResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.admin.response.SalesReportDTO;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.product.LowStockProductProjection;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.UserAccountIsAlreadyActivatedException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.UserAccountIsAlreadyDeactivatedException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.UserCannotChangeOwnRoleException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.UserNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.UserRoleIsAlreadyAdminException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.auth.UserRoleIsAlreadyCustomerException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class AdminPanelServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminPanelService adminPanelService;

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {
        @Test
        @DisplayName("Should Return All Users")
        void shouldReturnAllUsers() {
            // given
            UUID userId = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
            LocalDateTime updatedAt = LocalDateTime.now();

            User user = User.builder()
                    .userId(userId)
                    .username("Miguel")
                    .email("miguel@email.com")
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .active(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<User> users = new PageImpl<>(List.of(user));

            given(userRepository.findAll(pageable)).willReturn(users);

            // when
            Page<GetUsersResponseDTO> result = adminPanelService.getAllUsers(pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);

            GetUsersResponseDTO dto = result.getContent().getFirst();

            assertThat(dto.userId()).isEqualTo(userId);
            assertThat(dto.username()).isEqualTo("Miguel");
            assertThat(dto.email()).isEqualTo("miguel@email.com");
            assertThat(dto.userRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
            assertThat(dto.active()).isTrue();
            assertThat(dto.createdAt()).isEqualTo(createdAt);
            assertThat(dto.updatedAt()).isEqualTo(updatedAt);

            then(userRepository).should().findAll(pageable);
        }
    }


    @Nested
    @DisplayName("getUserDetails")
    class GetUserDetails {
        UUID userId;
        UUID orderId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            orderId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return User Details With Order History")
        void shouldReturnUserDetailsWithOrderHistory() {
            // given
            LocalDateTime userCreatedAt = LocalDateTime.now().minusDays(10);
            LocalDateTime orderCreatedAt = LocalDateTime.now().minusDays(1);

            User user = User.builder()
                    .userId(userId)
                    .username("Miguel")
                    .email("miguel@email.com")
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .createdAt(userCreatedAt)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .totalAmount(BigDecimal.valueOf(150))
                    .createdAt(orderCreatedAt)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orders = new PageImpl<>(List.of(order));

            given(orderRepository.findByUserId(userId, pageable)).willReturn(orders);

            // when
            GetUserDetailsWithHistoryResponseDTO result = adminPanelService.getUserDetails(userId, pageable);

            // then
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo("Miguel");
            assertThat(result.email()).isEqualTo("miguel@email.com");
            assertThat(result.userRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
            assertThat(result.createdAt()).isEqualTo(userCreatedAt);

            assertThat(result.orders().getTotalElements()).isEqualTo(1);
            assertThat(result.orders().getContent().getFirst().orderId()).isEqualTo(orderId);
            assertThat(result.orders().getContent().getFirst().totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(result.orders().getContent().getFirst().createdAt()).isEqualTo(orderCreatedAt);

            then(orderRepository).should().findByUserId(userId, pageable);
        }
    }


    @Nested
    @DisplayName("changeUserRole")
    class ChangeUserRole {
        UUID userId;
        UUID loggedUserId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            loggedUserId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When User Not Found")
        void shouldThrowExceptionWhenUserNotFound() {
            // given
            User loggedUser = User.builder()
                    .userId(loggedUserId)
                    .build();

            ChangeUserRoleRequestDTO requestDTO = new ChangeUserRoleRequestDTO(UserRole.ROLE_ADMIN);

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when + then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                adminPanelService.changeUserRole(userId, requestDTO, loggedUser);
            });

            assertThat(exception.getMessage()).isEqualTo("User Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When User Tries To Change Own Role")
        void shouldThrowExceptionWhenUserTriesToChangeOwnRole() {
            // given
            User loggedUser = User.builder()
                    .userId(userId)
                    .build();

            User user = User.builder()
                    .userId(userId)
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .build();

            ChangeUserRoleRequestDTO requestDTO = new ChangeUserRoleRequestDTO(UserRole.ROLE_ADMIN);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when + then
            UserCannotChangeOwnRoleException exception = assertThrows(UserCannotChangeOwnRoleException.class, () -> {
                adminPanelService.changeUserRole(userId, requestDTO, loggedUser);
            });

            assertThat(exception.getMessage()).isEqualTo("User Cannot Change Their Own Role!");
        }


        @Test
        @DisplayName("Should Throw Exception When User Is Already Admin")
        void shouldThrowExceptionWhenUserIsAlreadyAdmin() {
            // given
            User loggedUser = User.builder()
                    .userId(loggedUserId)
                    .build();

            User user = User.builder()
                    .userId(userId)
                    .userRole(UserRole.ROLE_ADMIN)
                    .build();

            ChangeUserRoleRequestDTO requestDTO = new ChangeUserRoleRequestDTO(UserRole.ROLE_ADMIN);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when + then
            UserRoleIsAlreadyAdminException exception = assertThrows(UserRoleIsAlreadyAdminException.class, () -> {
                adminPanelService.changeUserRole(userId, requestDTO, loggedUser);
            });

            assertThat(exception.getMessage()).isEqualTo("User Role Is Already Admin");
        }


        @Test
        @DisplayName("Should Throw Exception When User Is Already Customer")
        void shouldThrowExceptionWhenUserIsAlreadyCustomer() {
            // given
            User loggedUser = User.builder()
                    .userId(loggedUserId)
                    .build();

            User user = User.builder()
                    .userId(userId)
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .build();

            ChangeUserRoleRequestDTO requestDTO = new ChangeUserRoleRequestDTO(UserRole.ROLE_CUSTOMER);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when + then
            UserRoleIsAlreadyCustomerException exception = assertThrows(UserRoleIsAlreadyCustomerException.class, () -> {
                adminPanelService.changeUserRole(userId, requestDTO, loggedUser);
            });

            assertThat(exception.getMessage()).isEqualTo("User Role Is Already Customer");
        }


        @Test
        @DisplayName("Should Change User Role Successfully")
        void shouldChangeUserRoleSuccessfully() {
            // given
            User loggedUser = User.builder()
                    .userId(loggedUserId)
                    .build();

            User user = User.builder()
                    .userId(userId)
                    .userRole(UserRole.ROLE_CUSTOMER)
                    .build();

            ChangeUserRoleRequestDTO requestDTO = new ChangeUserRoleRequestDTO(UserRole.ROLE_ADMIN);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            adminPanelService.changeUserRole(userId, requestDTO, loggedUser);

            // then
            assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_ADMIN);
            then(userRepository).should().save(user);
        }
    }


    @Nested
    @DisplayName("activeUserAccount")
    class ActiveUserAccount {
        UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When User Not Found")
        void shouldThrowExceptionWhenUserNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when + then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                adminPanelService.activeUserAccount(userId);
            });

            assertThat(exception.getMessage()).isEqualTo("User Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When User Account Is Already Activated")
        void shouldThrowExceptionWhenUserAccountIsAlreadyActivated() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .active(true)
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when + then
            UserAccountIsAlreadyActivatedException exception = assertThrows(UserAccountIsAlreadyActivatedException.class, () -> {
                adminPanelService.activeUserAccount(userId);
            });

            assertThat(exception.getMessage()).isEqualTo("User Account Is Already Activated!");
        }


        @Test
        @DisplayName("Should Activate User Account Successfully")
        void shouldActivateUserAccountSuccessfully() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .active(false)
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            adminPanelService.activeUserAccount(userId);

            // then
            assertThat(user.getActive()).isTrue();
            then(userRepository).should().save(user);
        }
    }


    @Nested
    @DisplayName("disableUserAccount")
    class DisableUserAccount {
        UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When User Not Found")
        void shouldThrowExceptionWhenUserNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when + then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                adminPanelService.disableUserAccount(userId);
            });

            assertThat(exception.getMessage()).isEqualTo("User Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When User Account Is Already Deactivated")
        void shouldThrowExceptionWhenUserAccountIsAlreadyDeactivated() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .active(false)
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when + then
            UserAccountIsAlreadyDeactivatedException exception = assertThrows(UserAccountIsAlreadyDeactivatedException.class, () -> {
                adminPanelService.disableUserAccount(userId);
            });

            assertThat(exception.getMessage()).isEqualTo("User Account Is Already Deactivated!");
        }


        @Test
        @DisplayName("Should Disable User Account Successfully")
        void shouldDisableUserAccountSuccessfully() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .active(true)
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            adminPanelService.disableUserAccount(userId);

            // then
            assertThat(user.getActive()).isFalse();
            then(userRepository).should().save(user);
        }
    }


    @Nested
    @DisplayName("getLowStockProducts")
    class GetLowStockProducts {
        @Test
        @DisplayName("Should Return Low Stock Products")
        void shouldReturnLowStockProducts() {
            // given
            UUID productId = UUID.randomUUID();

            LowStockProductProjection product = mock(LowStockProductProjection.class);

            given(product.getProductId()).willReturn(productId);
            given(product.getName()).willReturn("Product");
            given(product.getStockQuantity()).willReturn(3);

            given(productRepository.findLowStockProducts(5)).willReturn(List.of(product));

            // when
            List<GetAllLowStockProductsDTO> result = adminPanelService.getLowStockProducts(5);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().productId()).isEqualTo(productId);
            assertThat(result.getFirst().name()).isEqualTo("Product");
            assertThat(result.getFirst().stockQuantity()).isEqualTo(3);

            then(productRepository).should().findLowStockProducts(5);
        }
    }

    @Nested
    @DisplayName("getTotalRevenuePerPeriod")
    class GetTotalRevenuePerPeriod {
        @Test
        @DisplayName("Should Return Sales Report")
        void shouldReturnSalesReport() {
            // given
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 20);

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);

            given(orderRepository.getTotalRevenueByPeriod(start, end))
                    .willReturn(BigDecimal.valueOf(1000));

            given(orderRepository.countOrdersByPeriod(start, end))
                    .willReturn(5L);

            // when
            SalesReportDTO result = adminPanelService.getTotalRevenuePerPeriod(startDate, endDate);

            // then
            assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(result.totalOrders()).isEqualTo(5L);
            assertThat(result.startDate()).isEqualTo(startDate);
            assertThat(result.endDate()).isEqualTo(endDate);

            then(orderRepository).should().getTotalRevenueByPeriod(start, end);
            then(orderRepository).should().countOrdersByPeriod(start, end);
        }
    }
}
