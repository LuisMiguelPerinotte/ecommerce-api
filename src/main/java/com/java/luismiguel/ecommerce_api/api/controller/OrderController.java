package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.order.request.CreateOrderRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.order.response.CreatedOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.order.response.GetAllUserOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.api.dto.order.response.GetOrderResponseDTO;
import com.java.luismiguel.ecommerce_api.application.order.OrderService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create Order", description = "Create an order from the authenticated user's cart or provided payload.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreatedOrderResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid order payload", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart or address not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Insufficient stock for one or more products", content = @Content)
    })
    public ResponseEntity<CreatedOrderResponseDTO> createOrder(
            @Valid
            @RequestBody CreateOrderRequestDTO createOrderRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(orderService.createOrder(createOrderRequestDTO, user), HttpStatus.CREATED);
    }


    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get User Orders", description = "Return a pageable list of the authenticated user's orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of user orders", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetAllUserOrderResponseDTO.class)))
    })
    public ResponseEntity<Page<GetAllUserOrderResponseDTO>> getUserOrders(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(orderService.getAllUserOrders(pageable, user), HttpStatus.OK);
    }


    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get Order by ID", description = "Return detailed information about a specific order by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetOrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    public ResponseEntity<GetOrderResponseDTO> getOrderById(@PathVariable UUID orderId) {
        return new ResponseEntity<>(orderService.getOrderById(orderId), HttpStatus.OK);
    }


    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel Order", description = "Cancel an order by ID if cancellation rules apply.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order canceled (no content)"),
            @ApiResponse(responseCode = "422", description = "Order cannot be cancelled in its current state", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    public ResponseEntity<Void> cancelOrderById(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User user
    ) {
        orderService.cancelOrderById(orderId, user);
        return ResponseEntity.noContent().build();
    }
}
