package com.java.luismiguel.ecommerce_api.application.address;

import com.java.luismiguel.ecommerce_api.api.dto.address.request.CreateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.request.UpdateAddressRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.request.AddressDataFromRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.CreatedAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAllUserAddressesResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.address.Address;
import com.java.luismiguel.ecommerce_api.domain.address.AddressRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressAlreadyExistsException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressIsAlreadyDefaultException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.InvalidZipCodeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }


    public CreatedAddressResponseDTO createAddress(CreateAddressRequestDTO createAddressRequestDTO, User user) {
        String street = normalize(createAddressRequestDTO.street());
        Optional<Address> existingAddress = addressRepository.findByUserUserIdAndStreetAndHouseNumber(
                user.getUserId(),
                street,
                createAddressRequestDTO.number().trim()
        );

        if (existingAddress.isPresent()) {
            if (existingAddress.get().getActive()) {
                throw new AddressAlreadyExistsException();
            }

            Address address = activateExistingAddress(existingAddress.get(), createAddressRequestDTO);
            return toCreatedAddressDTO(addressRepository.save(address));

        } else {
            AddressDataFromRequestDTO addressData = validateZipCodeAndGetAddressData(createAddressRequestDTO.zipCode());

            Address address = Address.builder()
                    .user(user)
                    .street(street)
                    .houseNumber(createAddressRequestDTO.number().trim())
                    .neighborhood(normalize(createAddressRequestDTO.neighborhood()))
                    .city(addressData.localidade())
                    .state(addressData.uf())
                    .zipCode(addressData.cep())
                    .isDefault(Boolean.FALSE)
                    .active(Boolean.TRUE)
                    .build();

            Optional.ofNullable(createAddressRequestDTO.complement())
                    .map(String::trim)
                    .ifPresent(address::setComplement);

            Address savedAddress = addressRepository.save(address);
            return toCreatedAddressDTO(savedAddress);
        }
    }


    public Page<GetAllUserAddressesResponseDTO> getUserAddresses(User user, Pageable pageable) {
        return addressRepository.findByUserUserIdAndActiveTrue(user.getUserId(), pageable)
                .map(address -> new GetAllUserAddressesResponseDTO(
                        address.getAddressId(),
                        address.getStreet(),
                        address.getHouseNumber(),
                        address.getCity(),
                        address.getState(),
                        address.getIsDefault()
                ));
    }


    public GetAddressResponseDTO getAddressById(UUID addressId, UUID userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(AddressNotFoundException::new);

        if (!address.getActive()) {
            throw new AddressNotFoundException();
        }

        if (!address.getUser().getUserId().equals(userId)) {
            throw new AddressNotFoundException();
        }

        return new GetAddressResponseDTO(
                address.getAddressId(),
                address.getStreet(),
                address.getHouseNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getIsDefault()
        );
    }


    public void deleteAddressById(UUID addressId) {
        Address address = addressRepository.findById(addressId)
                        .orElseThrow(AddressNotFoundException::new);

        if (!address.getActive()) {
            throw new AddressNotFoundException();
        }

        if (address.getIsDefault()){
            address.setIsDefault(Boolean.FALSE);
        }

        address.setActive(Boolean.FALSE);
        addressRepository.save(address);
    }


    public void updateAddress(UUID addressId, UpdateAddressRequestDTO updateAddressRequestDTO, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(AddressNotFoundException::new);

        String street = normalize(updateAddressRequestDTO.street());

        Optional<Address> existingAddress = addressRepository.findByUserUserIdAndStreetAndHouseNumber(
                user.getUserId(),
                street,
                updateAddressRequestDTO.number().trim()
        );

        if (existingAddress.isPresent()) {
            throw new AddressAlreadyExistsException();
        }

        if (!address.getActive()) {
            throw new AddressNotFoundException();
        }

        if (updateAddressRequestDTO.zipCode() != null) {
            if (!updateAddressRequestDTO.zipCode().isBlank()) {
                AddressDataFromRequestDTO addressData = validateZipCodeAndGetAddressData(updateAddressRequestDTO.zipCode());

                address.setZipCode(addressData.cep());
                address.setCity(addressData.localidade());
                address.setState(addressData.uf());
            }
        }

        Optional.ofNullable(street)
                .ifPresent(address::setStreet);

        Optional.ofNullable(updateAddressRequestDTO.neighborhood())
                .map(AddressService::normalize)
                .ifPresent(address::setNeighborhood);

        Optional.ofNullable(updateAddressRequestDTO.number())
                .map(String::trim)
                .ifPresent(address::setHouseNumber);

        Optional.ofNullable(updateAddressRequestDTO.complement())
                .map(String::trim)
                .ifPresent(address::setComplement);

        addressRepository.save(address);
    }


    public void setDefaultAddress(UUID addressId, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(AddressNotFoundException::new);

        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new AddressNotFoundException();
        }

        if (address.getIsDefault()) {
            throw new AddressIsAlreadyDefaultException();
        }

        Optional<Address> optionalAddressDefault = addressRepository.
                findByUserUserIdAndIsDefaultTrue(user.getUserId());

        optionalAddressDefault.ifPresent(oldDefault -> {
            oldDefault.setIsDefault(Boolean.FALSE);
            addressRepository.save(oldDefault);
        });

        address.setIsDefault(Boolean.TRUE);
        addressRepository.save(address);
    }


    // Private Methods
    private AddressDataFromRequestDTO requestToApiViaCep(String zipCode) {
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(
                String.format("https://viacep.com.br/ws/%s/json", zipCode),
                AddressDataFromRequestDTO.class);
    }


    private static CreatedAddressResponseDTO toCreatedAddressDTO(Address savedAddress) {
        return new CreatedAddressResponseDTO(
                savedAddress.getAddressId(),
                savedAddress.getStreet(),
                savedAddress.getHouseNumber(),
                savedAddress.getComplement(),
                savedAddress.getNeighborhood(),
                savedAddress.getCity(),
                savedAddress.getState(),
                savedAddress.getZipCode(),
                savedAddress.getIsDefault()
        );
    }


    private static String normalize(String t) {
        if (t == null) return null;

        return Normalizer.normalize(t, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[,.;:]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }


    private Address activateExistingAddress(Address address, CreateAddressRequestDTO createAddressRequestDTO) {
        AddressDataFromRequestDTO addressData = validateZipCodeAndGetAddressData(createAddressRequestDTO.zipCode());

        address.setNeighborhood(normalize(createAddressRequestDTO.neighborhood()));
        address.setState(addressData.uf());
        address.setCity(addressData.localidade());
        address.setZipCode(addressData.cep());

        Optional.ofNullable(createAddressRequestDTO.complement())
                .map(String::trim)
                .ifPresent(address::setComplement);

        address.setActive(Boolean.TRUE);
        return address;
    }


    private AddressDataFromRequestDTO validateZipCodeAndGetAddressData(String zipCode) {
        AddressDataFromRequestDTO addressData = requestToApiViaCep(zipCode);

        if (addressData.erro() != null) {
            throw new InvalidZipCodeException();
        }

        return addressData;
    }
}
