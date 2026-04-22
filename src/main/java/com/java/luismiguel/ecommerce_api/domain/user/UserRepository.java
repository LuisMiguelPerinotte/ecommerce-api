package com.java.luismiguel.ecommerce_api.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.userRole = 'ROLE_CUSTOMER'
            AND u.active = true
""")
    Long countActiveCustomers();
}
