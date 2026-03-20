package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.request.CreateOrderRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.CreatedOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.GetAllUserOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.response.GetOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.application.order.OrderService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Pedidos", description = "")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Criar Pedido", description = "")
    public ResponseEntity<CreatedOrderResponseDTO> createOrder(
            @Valid
            @RequestBody CreateOrderRequestDTO createOrderRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(orderService.createOrder(createOrderRequestDTO, user), HttpStatus.CREATED);
    }


    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Obter Pedidos", description = "")
    public ResponseEntity<Page<GetAllUserOrderResponseDTO>> getUserOrders(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(orderService.getAllUserOrders(pageable, user), HttpStatus.OK);
    }


    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Obter Pedido Pelo Id", description = "")
    public ResponseEntity<GetOrderResponseDTO> getOrderById(@PathVariable UUID orderId) {
        return new ResponseEntity<>(orderService.getOrderById(orderId), HttpStatus.OK);
    }


    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancelar Pedido", description = "")
    public ResponseEntity<Void> cancelOrderById(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User user
    ) {
        orderService.cancelOrderById(orderId, user);
        return ResponseEntity.noContent().build();
    }
}
