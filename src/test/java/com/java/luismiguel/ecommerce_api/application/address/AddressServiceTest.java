package com.java.luismiguel.ecommerce_api.application.address;

import com.java.luismiguel.ecommerce_api.api.dto.address.request.AddressDataFromRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.request.CreateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.request.UpdateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.CreatedAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAllUserAddressesResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.address.Address;
import com.java.luismiguel.ecommerce_api.domain.address.AddressRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.infrastructure.client.viacep.ViaCepClient;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressAlreadyExistsException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressIsAlreadyDefaultException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.InvalidZipCodeException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {
    @InjectMocks
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ViaCepClient viaCepClient;

    @Nested
    @DisplayName("createAddress")
    class CreateAddress {
        UUID addressId;
        UUID userId;
        CreateAddressRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            addressId = UUID.randomUUID();
            userId = UUID.randomUUID();
            requestDTO = new CreateAddressRequestDTO(
                    "street",
                    "neighborhood",
                    "number",
                    "complement",
                    "00000000"
            );
        }


        @Test
        @DisplayName("Should Reactivate Inactive Address")
        void shouldReactivateInactiveAddress() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Address address = Address.builder()
                    .addressId(addressId)
                    .street("street")
                    .houseNumber("number")
                    .neighborhood("old neighborhood")
                    .city("old city")
                    .state("old state")
                    .zipCode("00000-000")
                    .complement("old complement")
                    .active(false)
                    .isDefault(false)
                    .build();

            Address savedAddress = Address.builder()
                    .addressId(addressId)
                    .street("street")
                    .houseNumber("number")
                    .neighborhood("neighborhood")
                    .city("São Paulo")
                    .state("SP")
                    .zipCode("01001-000")
                    .complement("complement")
                    .active(true)
                    .isDefault(false)
                    .build();

            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, "street", "number"))
                    .willReturn(Optional.of(address));

            given(viaCepClient.getAddressData(requestDTO.zipCode()))
                    .willReturn(validAddressData());

            given(addressRepository.save(address))
                    .willReturn(savedAddress);

            // when
            CreatedAddressResponseDTO result = addressService.createAddress(requestDTO, user);

            // then
            assertThat(result.addressId()).isEqualTo(addressId);
            assertThat(result.neighborhood()).isEqualTo("neighborhood");
            assertThat(result.city()).isEqualTo("São Paulo");
            assertThat(result.state()).isEqualTo("SP");
            assertThat(result.zipCode()).isEqualTo("01001-000");
            assertThat(result.complement()).isEqualTo("complement");
            assertThat(result.isDefault()).isFalse();

            assertThat(address.getActive()).isTrue();

            then(addressRepository).should().save(address);
        }


        @Test
        @DisplayName("Should Throw Exception When Address Already Exists")
        void shouldThrowExceptionWhenAddressAlreadyExists() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Address address = Address.builder()
                    .addressId(addressId)
                    .active(true)
                    .build();

            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, requestDTO.street(), requestDTO.number()))
                    .willReturn(Optional.of(address));

            // when + then
            AddressAlreadyExistsException exception = assertThrows(AddressAlreadyExistsException.class, () -> {
                addressService.createAddress(requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Already Exists!");
        }


        @Test
        @DisplayName("Should Create Address Successfully")
        void shouldCreateAddressSuccessfully() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Address savedAddress = Address.builder()
                    .addressId(addressId)
                    .user(user)
                    .street("street")
                    .neighborhood("neighborhood")
                    .houseNumber("number")
                    .complement("complement")
                    .city("São Paulo")
                    .state("SP")
                    .zipCode("01001-000")
                    .active(true)
                    .isDefault(false)
                    .build();

            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, "street", "number"))
                    .willReturn(Optional.empty());

            given(viaCepClient.getAddressData(requestDTO.zipCode())).willReturn(validAddressData());

            given(addressRepository.save(any(Address.class))).willReturn(savedAddress);

            // when
            CreatedAddressResponseDTO result = addressService.createAddress(requestDTO, user);

            // then
            assertThat(result.addressId()).isEqualTo(addressId);
            assertThat(result.street()).isEqualTo("street");
            assertThat(result.neighborhood()).isEqualTo("neighborhood");
            assertThat(result.number()).isEqualTo("number");
            assertThat(result.complement()).isEqualTo("complement");
            assertThat(result.city()).isEqualTo("São Paulo");
            assertThat(result.state()).isEqualTo("SP");
            assertThat(result.zipCode()).isEqualTo("01001-000");
            assertThat(result.isDefault()).isFalse();

            then(addressRepository).should().save(any(Address.class));
        }


        @Test
        @DisplayName("Should Throw Exception When Zip Code Is Invalid")
        void shouldThrowExceptionWhenZipCodeIsInvalid() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, "street", "number"))
                    .willReturn(Optional.empty());

            given(viaCepClient.getAddressData(requestDTO.zipCode()))
                    .willReturn(invalidAddressData());

            // when + then
            InvalidZipCodeException exception = assertThrows(InvalidZipCodeException.class, () -> {
                addressService.createAddress(requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Invalid ZipCode!");
        }
    }


    @Nested
    @DisplayName("getUserAddresses")
    class GetUserAddresses {
        UUID userId;
        UUID addressId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            addressId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return Active User Addresses")
        void shouldReturnActiveUserAddresses() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            String street = "street";
            String houseNumber = "00";
            String city = "city";
            String state = "state";

            Address address = Address.builder()
                    .addressId(addressId)
                    .street(street)
                    .houseNumber(houseNumber)
                    .city(city)
                    .state(state)
                    .active(true)
                    .isDefault(false)
                    .user(user)
                    .build();

            Page<Address> addresses = new PageImpl<>(List.of(address));
            Pageable pageable = PageRequest.of(0, 10);

            given(addressRepository.findByUserUserIdAndActiveTrue(user.getUserId(), pageable))
                    .willReturn(addresses);

            // when
            Page<GetAllUserAddressesResponseDTO> result = addressService.getUserAddresses(user, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);

            GetAllUserAddressesResponseDTO dto = result.getContent().getFirst();

            assertThat(dto.addressId()).isEqualTo(addressId);
            assertThat(dto.street()).isEqualTo(street);
            assertThat(dto.number()).isEqualTo(houseNumber);
            assertThat(dto.city()).isEqualTo(city);
            assertThat(dto.state()).isEqualTo(state);
            assertThat(dto.isDefault()).isFalse();

            then(addressRepository).should()
                    .findByUserUserIdAndActiveTrue(userId, pageable);
        }
    }


    @Nested
    @DisplayName("getAddressById")
    class GetAddressById {
        UUID addressId;
        UUID userId;

        @BeforeEach
        void setUp() {
            addressId = UUID.randomUUID();
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Return Address By Id Successfully")
        void shouldReturnAddressByIdSuccessfully() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            String street = "street";
            String houseNumber = "00";
            String city = "city";
            String state = "state";
            String complement = "complement";
            String neighborhood = "neighborhood";
            String zipCode = "01001-000";

            Address address = Address.builder()
                    .addressId(addressId)
                    .street(street)
                    .houseNumber(houseNumber)
                    .complement(complement)
                    .neighborhood(neighborhood)
                    .zipCode(zipCode)
                    .city(city)
                    .state(state)
                    .active(true)
                    .user(user)
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            GetAddressResponseDTO result = addressService.getAddressById(addressId, userId);

            assertThat(result.addressId()).isEqualTo(addressId);
            assertThat(result.street()).isEqualTo(street);
            assertThat(result.number()).isEqualTo(houseNumber);
            assertThat(result.complement()).isEqualTo(complement);
            assertThat(result.neighborhood()).isEqualTo(neighborhood);
            assertThat(result.zipCode()).isEqualTo(zipCode);
            assertThat(result.city()).isEqualTo(city);
            assertThat(result.state()).isEqualTo(state);
            assertThat(result.isDefault()).isFalse();
        }


        @Test
        @DisplayName("Should Throw Exception When Address Not Found")
        void shouldThrowExceptionWhenAddressNotFound() {
            // given
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.getAddressById(addressId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Not Activated")
        void shouldThrowExceptionWhenAddressNotActivated() {
            // given
            Address address = Address.builder()
                    .active(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.getAddressById(addressId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Belongs To Another User")
        void shouldThrowExceptionWhenAddressBelongsToAnotherUser() {
            // given
            User user = User.builder()
                    .userId(UUID.randomUUID())
                    .build();

            Address address = Address.builder()
                    .user(user)
                    .active(true)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.getAddressById(addressId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }
    }


    @Nested
    @DisplayName("deleteAddressById")
    class DeleteAddressById {
        UUID addressId;

        @BeforeEach
        void setUp() {
            addressId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Address Not Found")
        void shouldThrowExceptionWhenAddressNotFound() {
            // given
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.deleteAddressById(addressId);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Not Activated")
        void shouldThrowExceptionWhenAddressNotActivated() {
            // given
            Address address = Address.builder()
                    .active(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.deleteAddressById(addressId);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Soft Delete Address By Id Successfully And Set Default False")
        void shouldSoftDeleteAddressByIdSuccessfullyAndSetDefaultFalse() {
            // given
            Address address = Address.builder()
                    .addressId(addressId)
                    .active(true)
                    .isDefault(true)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            addressService.deleteAddressById(addressId);

            // then
            assertThat(address.getActive()).isFalse();
            assertThat(address.getIsDefault()).isFalse();
            then(addressRepository).should().save(address);
        }
    }


    @Nested
    @DisplayName("updateAddress")
    class UpdateAddress {
        UUID addressId;
        UUID userId;
        User user;
        UpdateAddressRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            addressId = UUID.randomUUID();
            userId = UUID.randomUUID();

            user = User.builder()
                    .userId(userId)
                    .build();

            requestDTO = new UpdateAddressRequestDTO(
                    "new street",
                    "new neighborhood",
                    "123",
                    "new complement",
                    "11111111"
            );
        }

        @Test
        @DisplayName("Should Throw Exception When Address Not Found")
        void shouldThrowExceptionWhenAddressNotFound() {
            // given
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.updateAddress(addressId, requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Not Activated")
        void shouldThrowExceptionWhenAddressNotActivated() {
            // given
            Address address = Address.builder()
                    .active(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, "new street", "123"))
                    .willReturn(Optional.empty());

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.updateAddress(addressId, requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Already Exists")
        void shouldThrowExceptionWhenAddressAlreadyExists() {
            // given
            Address address = Address.builder()
                    .addressId(addressId)
                    .active(true)
                    .build();

            Address existingAddress = Address.builder()
                    .addressId(UUID.randomUUID())
                    .active(true)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, "new street", "123"))
                    .willReturn(Optional.of(existingAddress));

            // when + then
            AddressAlreadyExistsException exception = assertThrows(AddressAlreadyExistsException.class, () -> {
                addressService.updateAddress(addressId, requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Already Exists!");
        }


        @Test
        @DisplayName("Should Throw Exception When Zip Code Is Invalid")
        void shouldThrowExceptionWhenZipCodeIsInvalid() {
            // given
            Address address = Address.builder()
                    .addressId(addressId)
                    .active(true)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, "new street", "123"))
                    .willReturn(Optional.empty());
            given(viaCepClient.getAddressData(requestDTO.zipCode()))
                    .willReturn(invalidAddressData());

            // when + then
            InvalidZipCodeException exception = assertThrows(InvalidZipCodeException.class, () -> {
                addressService.updateAddress(addressId, requestDTO, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Invalid ZipCode!");
        }


        @Test
        @DisplayName("Should Update Address Successfully")
        void shouldUpdateAddressSuccessfully() {
            // given
            Address address = Address.builder()
                    .addressId(addressId)
                    .street("old street")
                    .neighborhood("old neighborhood")
                    .houseNumber("00")
                    .complement("old complement")
                    .city("old city")
                    .state("old state")
                    .zipCode("00000-000")
                    .active(true)
                    .user(user)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
            given(addressRepository.findByUserUserIdAndStreetAndHouseNumber(userId, "new street", "123"))
                    .willReturn(Optional.empty());
            given(viaCepClient.getAddressData(requestDTO.zipCode()))
                    .willReturn(validAddressData());

            // when
            addressService.updateAddress(addressId, requestDTO, user);

            // then
            assertThat(address.getStreet()).isEqualTo("new street");
            assertThat(address.getNeighborhood()).isEqualTo("new neighborhood");
            assertThat(address.getHouseNumber()).isEqualTo("123");
            assertThat(address.getComplement()).isEqualTo("new complement");
            assertThat(address.getCity()).isEqualTo("São Paulo");
            assertThat(address.getState()).isEqualTo("SP");
            assertThat(address.getZipCode()).isEqualTo("01001-000");

            then(addressRepository).should().save(address);
        }
    }


    @Nested
    @DisplayName("SetDefaultAddress")
    class SetDefaultAddress {
        UUID addressId;
        UUID userId;
        User user;

        @BeforeEach
        void setUp() {
            addressId = UUID.randomUUID();
            userId = UUID.randomUUID();

            user = User.builder()
                    .userId(userId)
                    .build();
        }

        @Test
        @DisplayName("Should Throw Exception When Address Not Found")
        void shouldThrowExceptionWhenAddressNotFound() {
            // given
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.setDefaultAddress(addressId, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Belongs To Another User")
        void shouldThrowExceptionWhenAddressBelongsToAnotherUser() {
            // given
            User user1 = User.builder()
                    .userId(UUID.randomUUID())
                    .build();

            Address address = Address.builder()
                    .user(user1)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when + then
            AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> {
                addressService.setDefaultAddress(addressId, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Address Already Default")
        void shouldThrowExceptionWhenAddressAlreadyDefault() {
            // given
            Address address = Address.builder()
                    .user(user)
                    .isDefault(true)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when + then
            AddressIsAlreadyDefaultException exception = assertThrows(AddressIsAlreadyDefaultException.class, () -> {
                addressService.setDefaultAddress(addressId, user);
            });

            assertThat(exception.getMessage()).isEqualTo("Address Is Already Default!");
        }


        @Test
        @DisplayName("Should Set Default Address Successfully")
        void shouldSetDefaultAddressSuccessfully() {
            // given
            Address address = Address.builder()
                    .addressId(addressId)
                    .user(user)
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
            given(addressRepository.findByUserUserIdAndIsDefaultTrue(userId))
                    .willReturn(Optional.empty());

            // when
            addressService.setDefaultAddress(addressId, user);

            // then
            assertThat(address.getIsDefault()).isTrue();
            then(addressRepository).should().save(address);
        }


        @Test
        @DisplayName("Should Replace Old Default Address")
        void shouldReplaceOldDefaultAddress() {
            // given
            Address oldDefaultAddress = Address.builder()
                    .addressId(UUID.randomUUID())
                    .user(user)
                    .isDefault(true)
                    .build();

            Address newDefaultAddress = Address.builder()
                    .addressId(addressId)
                    .user(user)
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(newDefaultAddress));
            given(addressRepository.findByUserUserIdAndIsDefaultTrue(userId))
                    .willReturn(Optional.of(oldDefaultAddress));

            // when
            addressService.setDefaultAddress(addressId, user);

            // then
            assertThat(oldDefaultAddress.getIsDefault()).isFalse();
            assertThat(newDefaultAddress.getIsDefault()).isTrue();

            then(addressRepository).should().save(oldDefaultAddress);
            then(addressRepository).should().save(newDefaultAddress);
        }
    }

    // Helpers
    private AddressDataFromRequestDTO validAddressData() {
        return new AddressDataFromRequestDTO(
                "01001-000",
                "Praça da Sé",
                "",
                "",
                "Sé",
                "São Paulo",
                "SP",
                "São Paulo",
                "Sudeste",
                "3550308",
                "1004",
                "11",
                "7107",
                null
        );
    }


    private AddressDataFromRequestDTO invalidAddressData() {
        return new AddressDataFromRequestDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );
    }
}
