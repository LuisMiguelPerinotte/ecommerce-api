package com.java.luismiguel.ecommerce_api.domain.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Cart findByUserUserId(UUID userId);

    @Query("SELECT SUM(ci.quantity) as totalItems, SUM(ci.subtotal) as totalAmount " +
            "FROM CartItem ci WHERE ci.cart.id = :cartId")
    CartSummary getCartSummary(@Param("cartId") UUID cartId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.user.userId = :userId")
    Optional<Cart> findByUserUserIdWithItems(@Param("userId") UUID userId);
}
