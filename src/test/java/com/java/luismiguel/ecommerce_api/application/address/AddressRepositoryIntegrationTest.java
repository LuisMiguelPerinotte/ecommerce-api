package com.java.luismiguel.ecommerce_api.application.address;

import com.java.luismiguel.ecommerce_api.domain.address.Address;
import com.java.luismiguel.ecommerce_api.domain.address.AddressRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class AddressRepositoryIntegrationTest {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should Find Active Addresses By User Id")
    void shouldFindActiveAddressesByUserId() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        Address activeAddress = Address.builder()
                .user(user)
                .street("Rua A")
                .houseNumber("123")
                .complement("Casa")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01001-000")
                .isDefault(true)
                .active(true)
                .build();

        Address inactiveAddress = Address.builder()
                .user(user)
                .street("Rua B")
                .houseNumber("456")
                .complement("Apto")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01002-000")
                .isDefault(false)
                .active(false)
                .build();

        addressRepository.save(activeAddress);
        addressRepository.save(inactiveAddress);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Address> result = addressRepository.findByUserUserIdAndActiveTrue(user.getUserId(), pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getAddressId()).isEqualTo(activeAddress.getAddressId());
        assertThat(result.getContent().getFirst().getActive()).isTrue();
    }


    @Test
    @DisplayName("Should Not Return Inactive Addresses")
    void shouldNotReturnInactiveAddresses() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        Address inactiveAddress = Address.builder()
                .user(user)
                .street("Rua B")
                .houseNumber("456")
                .complement("Apto")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01002-000")
                .isDefault(false)
                .active(false)
                .build();

        addressRepository.save(inactiveAddress);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Address> result = addressRepository.findByUserUserIdAndActiveTrue(user.getUserId(), pageable);

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }


    @Test
    @DisplayName("Should Find Address By User Id Street And House Number")
    void shouldFindAddressByUserIdStreetAndHouseNumber() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        Address address = Address.builder()
                .user(user)
                .street("Rua A")
                .houseNumber("123")
                .complement("Casa")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01001-000")
                .isDefault(true)
                .active(true)
                .build();

        Address savedAddress = addressRepository.save(address);

        // when
        Optional<Address> result = addressRepository.findByUserUserIdAndStreetAndHouseNumber(
                user.getUserId(),
                "Rua A",
                "123"
        );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAddressId()).isEqualTo(savedAddress.getAddressId());
        assertThat(result.get().getStreet()).isEqualTo("Rua A");
        assertThat(result.get().getHouseNumber()).isEqualTo("123");
        assertThat(result.get().getUser().getUserId()).isEqualTo(user.getUserId());
    }


    @Test
    @DisplayName("Should Return Empty When Address By User Id Street And House Number Does Not Exist")
    void shouldReturnEmptyWhenAddressByUserIdStreetAndHouseNumberDoesNotExist() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        // when
        Optional<Address> result = addressRepository.findByUserUserIdAndStreetAndHouseNumber(
                user.getUserId(),
                "Rua Inexistente",
                "999"
        );

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("Should Find Default Address By User Id")
    void shouldFindDefaultAddressByUserId() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        Address defaultAddress = Address.builder()
                .user(user)
                .street("Rua A")
                .houseNumber("123")
                .complement("Casa")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01001-000")
                .isDefault(true)
                .active(true)
                .build();

        Address nonDefaultAddress = Address.builder()
                .user(user)
                .street("Rua B")
                .houseNumber("456")
                .complement("Apto")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01002-000")
                .isDefault(false)
                .active(true)
                .build();

        Address savedDefaultAddress = addressRepository.save(defaultAddress);
        addressRepository.save(nonDefaultAddress);

        // when
        Optional<Address> result = addressRepository.findByUserUserIdAndIsDefaultTrue(user.getUserId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAddressId()).isEqualTo(savedDefaultAddress.getAddressId());
        assertThat(result.get().getIsDefault()).isTrue();
    }


    @Test
    @DisplayName("Should Return Empty When User Does Not Have Default Address")
    void shouldReturnEmptyWhenUserDoesNotHaveDefaultAddress() {
        // given
        User user = saveUser("Miguel", "miguel@email.com");

        Address address = Address.builder()
                .user(user)
                .street("Rua B")
                .houseNumber("456")
                .complement("Apto")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01002-000")
                .isDefault(false)
                .active(true)
                .build();

        addressRepository.save(address);

        // when
        Optional<Address> result = addressRepository.findByUserUserIdAndIsDefaultTrue(user.getUserId());

        // then
        assertThat(result).isEmpty();
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
}