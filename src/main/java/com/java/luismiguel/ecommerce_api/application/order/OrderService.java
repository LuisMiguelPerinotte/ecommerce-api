package com.java.luismiguel.ecommerce_api.application.order;

import com.java.luismiguel.ecommerce_api.api.dto.request.CreateOrderRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.*;
import com.java.luismiguel.ecommerce_api.application.cart.CartService;
import com.java.luismiguel.ecommerce_api.domain.address.Address;
import com.java.luismiguel.ecommerce_api.domain.address.AddressRepository;
import com.java.luismiguel.ecommerce_api.domain.cart.Cart;
import com.java.luismiguel.ecommerce_api.domain.cart.CartItem;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderItem;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.product.Product;
import com.java.luismiguel.ecommerce_api.domain.product.ProductRepository;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.address.AddressNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.cart.CartIsEmptyException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order.OrderNotCancellableException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order.OrderNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.product.InsufficientProductStockException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, AddressRepository addressRepository, CartService cartService, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    @Transactional
    public CreatedOrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequestDTO, User user) {
        Cart userCart = user.getCart();

        if (userCart.getItems().isEmpty()) {
            throw new CartIsEmptyException();
        }

        List<CartItem> cartItems = userCart.getItems();

        cartItems.forEach(item ->
                productIsInStock(item.getProduct(), item.getQuantity()));

        Address address = addressRepository.findById(createOrderRequestDTO.addressId())
                .orElseThrow(AddressNotFoundException::new);

        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new AddressNotFoundException();
        }

        Order order = Order.builder()
                .user(user)
                .orderStatus(OrderStatus.PENDING)
                .shippingAddress(address)
                .userNotes(createOrderRequestDTO.userNotes())
                .build();

        List<OrderItem> orderItems = cartItemToOrderItem(cartItems, order);

        order.setItems(orderItems);
        order.setTotalAmount(sumTotalAmount(orderItems));

        Order savedNewOrder = orderRepository.save(order);

        decrementProductsStock(orderItems);
        cartService.cleanCart(user.getUserId());

        return new CreatedOrderResponseDTO(
                savedNewOrder.getOrderId(),
                savedNewOrder.getOrderStatus(),
                savedNewOrder.getItems().stream()
                        .map(orderItem -> new ListOrderItemResponseDTO(
                                orderItem.getOrderItemId(),
                                orderItem.getProduct().getProductId(),
                                orderItem.getProductName(),
                                orderItem.getUnitPrice(),
                                orderItem.getQuantity(),
                                orderItem.getSubtotal()
                        )).toList(),
                savedNewOrder.getTotalAmount(),
                savedNewOrder.getCreatedAt()
        );
    }


    public Page<GetAllUserOrderResponseDTO> getAllUserOrders(Pageable pageable, User user) {
        return orderRepository.findByUserUserId(user.getUserId(), pageable)
                .map(order -> new GetAllUserOrderResponseDTO(
                        order.getOrderId(),
                        order.getItems().stream()
                                .map(orderItem -> new ListOrderItemResponseDTO(
                                        orderItem.getOrderItemId(),
                                        orderItem.getProduct().getProductId(),
                                        orderItem.getProductName(),
                                        orderItem.getUnitPrice(),
                                        orderItem.getQuantity(),
                                        orderItem.getSubtotal()
                                )).toList(),
                        order.getOrderStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                ));

    }


    public GetOrderResponseDTO getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        Address address = order.getShippingAddress();
        GetAddressResponseDTO addressDTO = new GetAddressResponseDTO(
                address.getAddressId(),
                address.getStreet(),
                address.getComplement(),
                address.getState(),
                address.getZipCode(),
                address.getCountry()
        );

        return new GetOrderResponseDTO(
                order.getOrderId(),
                order.getItems().stream()
                        .map(orderItem -> new ListOrderItemResponseDTO(
                                orderItem.getOrderItemId(),
                                orderItem.getProduct().getProductId(),
                                orderItem.getProductName(),
                                orderItem.getUnitPrice(),
                                orderItem.getQuantity(),
                                orderItem.getSubtotal()
                        )).toList(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                addressDTO,
                order.getUserNotes(),
                order.getCreatedAt()
        );
    }


    public void cancelOrderById(UUID orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        if (order.getUser().getUserId().equals(user.getUserId())) {
            throw new OrderNotFoundException();
        }
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new OrderNotCancellableException();
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        incrementCancelledOrderProductsStock(order.getItems());
        orderRepository.save(order);
    }


    // Private Methods
    private static void productIsInStock(Product product, Integer quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientProductStockException(product.getName());
        }
    }


    private List<OrderItem> cartItemToOrderItem(List<CartItem> cartItems, Order newOrder) {
        return cartItems.stream()
                .map(item -> OrderItem.builder()
                        .order(newOrder)
                        .product(item.getProduct())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();
    }


    private BigDecimal sumTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private void decrementProductsStock(List<OrderItem> orderItems) {
        orderItems.forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - orderItem.getQuantity());
            productRepository.save(product);
        });
    }


    private void incrementCancelledOrderProductsStock(List<OrderItem> orderItems) {
        orderItems.forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        });
    }
}
