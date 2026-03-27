package com.java.luismiguel.ecommerce_api.domain.address;

import com.java.luismiguel.ecommerce_api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id")
    UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "street", nullable = false)
    String street;

    @Column(name = "house_number", nullable = false)
    String houseNumber;

    @Column(name = "complement")
    String complement;

    @Column(name = "neighborhood", nullable = false)
    String neighborhood;

    @Column(name = "city", nullable = false)
    String city;

    @Column(name = "state", nullable = false, length = 2)
    String state;

    @Column(name = "zip_code", nullable = false)
    String zipCode;

    @Column(name = "is_default", nullable = false)
    Boolean isDefault;

    @Column(name = "active", nullable = false)
    Boolean active;
}
