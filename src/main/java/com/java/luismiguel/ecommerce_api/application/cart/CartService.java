package com.java.luismiguel.ecommerce_api.application.cart;

import com.java.luismiguel.ecommerce_api.api.dto.request.AddCartItemRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.request.UpdateCartItemQuantityRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.AddedCartItemDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.GetCartResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.ListCartItemsResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.cart.*;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.CartIsEmptyException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.CartItemNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.InsufficientStockException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public GetCartResponseDTO getCart(UUID userId) {
        Cart cart = cartRepository.findByUserUserId(userId);

        CartSummary cartSummary = cartRepository.getCartSummary(cart.getCartId());

        return new GetCartResponseDTO(
                cart.getCartId(),
                cart.getItems().stream()
                        .map(cartItem -> new ListCartItemsResponseDTO(
                                cartItem.getCartItemId(),
                                cartItem.getProduct().getProductId(),
                                cartItem.getProduct().getName(),
                                cartItem.getUnitPrice(),
                                cartItem.getQuantity(),
                                cartItem.getSubtotal()
                        )).toList(),
                cartSummary.getTotalItems(),
                cartSummary.getTotalAmount()
        );
    }


    public AddedCartItemDTO addCartItem(AddCartItemRequestDTO addCartItemRequestDTO, UUID userId) {
        Cart cart = cartRepository.findByUserUserId(userId);

        Product product = productRepository.findById(addCartItemRequestDTO.productId())
                .orElseThrow(ProductNotFoundException::new);

        productIsActive(product);
        productIsInStock(product, addCartItemRequestDTO.quantity());

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getProductId()
                        .equals(addCartItemRequestDTO.productId()))
                .findFirst();

        BigDecimal subTotal = sumSubTotal(product.getPrice(), addCartItemRequestDTO.quantity());
        CartItem newCartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(addCartItemRequestDTO.quantity())
                .unitPrice(product.getPrice())
                .subtotal(subTotal)
                .build();

        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + addCartItemRequestDTO.quantity());
            cartItem.setSubtotal(sumSubTotal(cartItem.getUnitPrice(), cartItem.getQuantity()));

        } else {
            cart.getItems().addLast(newCartItem);
        }

        Cart savedCart = cartRepository.save(cart);
        CartItem savedCartItem = savedCart.getItems().getLast();

        return new AddedCartItemDTO(
                savedCartItem.getCartItemId(),
                savedCartItem.getProduct().getProductId(),
                savedCartItem.getProduct().getName(),
                savedCartItem.getUnitPrice(),
                savedCartItem.getQuantity(),
                savedCartItem.getSubtotal()
        );
    }


    public void updateCartItemQuantity(UpdateCartItemQuantityRequestDTO updateCartItemQuantityRequestDTO, UUID itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(CartItemNotFoundException::new);

        if (updateCartItemQuantityRequestDTO.quantity() == 0) {
            removeCartItem(itemId);
            return;
        }

        productIsInStock(cartItem.getProduct(), updateCartItemQuantityRequestDTO.quantity());

        cartItem.setQuantity(updateCartItemQuantityRequestDTO.quantity());
        cartItem.setSubtotal(sumSubTotal(cartItem.getUnitPrice(), cartItem.getQuantity()));
        cartItemRepository.save(cartItem);
    }


    public void removeCartItem(UUID itemId) {
        if (!cartItemRepository.existsById(itemId)) {
            throw new CartItemNotFoundException();
        }
        cartItemRepository.deleteById(itemId);
    }


    public void cleanCart(UUID userId) {
        Cart cart = cartRepository.findByUserUserId(userId);

        if (cart.getItems().isEmpty()) {
            throw new CartIsEmptyException();
        }

        cartItemRepository.deleteAllByCartId(cart.getCartId());
    }


    // Private Methods
    private static BigDecimal sumSubTotal(BigDecimal price, Integer quantity) {
        Double doublePrice = price.doubleValue();
        Double doubleQuantity = quantity.doubleValue();
        return BigDecimal.valueOf(doublePrice * doubleQuantity);
    }

    private static void productIsActive(Product product) {
        if (!product.getActive()) {
            throw new ProductNotFoundException();
        }
    }

    private static void productIsInStock(Product product, Integer quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException();
        }
    }
}
