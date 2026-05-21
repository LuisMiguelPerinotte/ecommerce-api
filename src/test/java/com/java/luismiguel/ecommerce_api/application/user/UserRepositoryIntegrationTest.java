package com.java.luismiguel.ecommerce_api.application.user;

import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.domain.user.UserRepository;
import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should Return True When Email Exists")
    void shouldReturnTrueWhenEmailExists() {
        // given
        User user = User.builder()
                .username("Miguel")
                .email("miguel@email.com")
                .password("encoded-password")
                .userRole(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        userRepository.save(user);

        // when
        Boolean result = userRepository.existsByEmail("miguel@email.com");

        // then
        assertThat(result).isTrue();
    }


    @Test
    @DisplayName("Should Return False When Email Does Not Exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // when
        Boolean result = userRepository.existsByEmail("notfound@email.com");

        // then
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Should Find User By Email")
    void shouldFindUserByEmail() {
        // given
        User user = User.builder()
                .username("Miguel")
                .email("miguel@email.com")
                .password("encoded-password")
                .userRole(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByEmail("miguel@email.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(result.get().getUsername()).isEqualTo("Miguel");
        assertThat(result.get().getEmail()).isEqualTo("miguel@email.com");
        assertThat(result.get().getPassword()).isEqualTo("encoded-password");
        assertThat(result.get().getUserRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(result.get().getActive()).isTrue();
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }


    @Test
    @DisplayName("Should Return Empty When Email Does Not Exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        // when
        Optional<User> result = userRepository.findByEmail("notfound@email.com");

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("Should Count Active Customers")
    void shouldCountActiveCustomers() {
        // given
        User activeCustomer = User.builder()
                .username("Active Customer")
                .email("active.customer@email.com")
                .password("encoded-password")
                .userRole(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        User inactiveCustomer = User.builder()
                .username("Inactive Customer")
                .email("inactive.customer@email.com")
                .password("encoded-password")
                .userRole(UserRole.ROLE_CUSTOMER)
                .active(false)
                .build();

        User activeAdmin = User.builder()
                .username("Active Admin")
                .email("active.admin@email.com")
                .password("encoded-password")
                .userRole(UserRole.ROLE_ADMIN)
                .active(true)
                .build();

        userRepository.save(activeCustomer);
        userRepository.save(inactiveCustomer);
        userRepository.save(activeAdmin);

        // when
        Long result = userRepository.countActiveCustomers();

        // then
        assertThat(result).isEqualTo(1L);
    }
}