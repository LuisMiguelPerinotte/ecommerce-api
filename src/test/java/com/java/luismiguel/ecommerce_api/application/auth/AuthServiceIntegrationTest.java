package com.java.luismiguel.ecommerce_api.application.auth;

import com.java.luismiguel.ecommerce_api.api.dto.auth.request.RegisterRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.auth.response.UserResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.cart.Cart;
import com.java.luismiguel.ecommerce_api.domain.cart.CartRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceIntegrationTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should Register New User And Create Cart")
    void shouldRegisterNewUserAndCreateCart() {
        // given
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "  MiguelUser  ",
                "  MIGUEL@EMAIL.COM  ",
                "Password@123"
        );

        // when
        UserResponseDTO response = authService.registerNewUser(requestDTO);

        // then
        assertThat(response.userId()).isNotNull();
        assertThat(response.username()).isEqualTo("MiguelUser");
        assertThat(response.email()).isEqualTo("miguel@email.com");
        assertThat(response.userRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(response.createdAt()).isNotNull();

        Optional<User> optionalUser = userRepository.findByEmail("miguel@email.com");

        assertThat(optionalUser).isPresent();

        User savedUser = optionalUser.get();

        assertThat(savedUser.getUserId()).isEqualTo(response.userId());
        assertThat(savedUser.getUsername()).isEqualTo("MiguelUser");
        assertThat(savedUser.getEmail()).isEqualTo("miguel@email.com");
        assertThat(savedUser.getUserRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(savedUser.getActive()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        assertThat(savedUser.getPassword()).isNotEqualTo("Password@123");
        assertThat(passwordEncoder.matches("Password@123", savedUser.getPassword())).isTrue();

        Cart cart = cartRepository.findByUserUserId(savedUser.getUserId());

        assertThat(cart).isNotNull();
        assertThat(cart.getCartId()).isNotNull();
        assertThat(cart.getUser().getUserId()).isEqualTo(savedUser.getUserId());
    }
}