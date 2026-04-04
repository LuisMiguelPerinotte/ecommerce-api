package com.java.luismiguel.ecommerce_api.domain.address;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    Page<Address> findByUserUserIdAndActiveTrue(UUID userId, Pageable pageable);

    Optional<Address> findByUserUserIdAndStreetAndHouseNumber(UUID userId, String street, String houseNumber);

    Optional<Address> findByUserUserIdAndIsDefaultTrue(UUID userId);
}
